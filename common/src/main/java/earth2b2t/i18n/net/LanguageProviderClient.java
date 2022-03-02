package earth2b2t.i18n.net;

import com.google.gson.Gson;
import earth2b2t.i18n.LanguageProvider;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class LanguageProviderClient implements LanguageProvider, Closeable {

    private static final int TIMEOUT = 10_000;
    private static final int RETRY_DELAY = 5_000;
    private final String host;
    private final int port;
    private final Gson gson;
    private Socket socket;
    private boolean closed;

    public LanguageProviderClient(String host, int port, Gson gson) {
        this.host = host;
        this.port = port;
        this.gson = gson;
    }

    public LanguageProviderClient(String host, int port) {
        this(host, port, new Gson());
    }

    public void refresh() {
        if (socket != null && socket.isConnected()) return;
        while (true) {
            try {
                Socket socket = new Socket();
                socket.setKeepAlive(true);
                socket.setSoTimeout(TIMEOUT);
                socket.connect(new InetSocketAddress(host, port));
                this.socket = socket;
                break;
            } catch (IOException e) {
                e.printStackTrace();

                // retry
                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
    }

    @Override
    public synchronized void update(UUID player, String preferred) {
        refresh();
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeInt(2);
            out.writeUTF(gson.toJson(new UpdateRequest(player, preferred)));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized List<String> get(UUID player) {
        refresh();
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeInt(1);
            out.writeUTF(gson.toJson(new SelectRequest(player)));
            out.flush();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            SelectResponse response = gson.fromJson(in.readUTF(), SelectResponse.class);
            return response.getLocales();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    @Override
    public void close() throws IOException {
        closed = true;
        socket.close();
    }

    public boolean isClosed() {
        return closed;
    }
}
