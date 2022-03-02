package earth2b2t.i18n.net;

import com.google.gson.Gson;
import earth2b2t.i18n.LanguageProvider;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class LanguageProviderServer implements LanguageProvider, Closeable {

    private static final int TIMEOUT = 10_000;
    private final ThreadLocal<Gson> gson = ThreadLocal.withInitial(Gson::new);
    private final LanguageProvider delegate;
    private final Runnable onClose;
    private boolean closed;

    private LanguageProviderServer(LanguageProvider delegate, Runnable onClose) {
        this.delegate = delegate;
        this.onClose = onClose;
    }

    public static LanguageProviderServer create(int port, Executor executor, LanguageProvider languageProvider) throws IOException {
        Collection<ServerSocket> sockets = Collections.newSetFromMap(new WeakHashMap<>());
        AtomicBoolean closed = new AtomicBoolean();

        LanguageProviderServer server = new LanguageProviderServer(languageProvider, () -> {
            closed.set(true);

            synchronized (sockets) {
                for (ServerSocket socket : sockets) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        ServerSocket serverSocket = new ServerSocket(port);

        // listen port
        executor.execute(() -> {
            while (true) {
                try {
                    synchronized (sockets) {
                        if (closed.get()) break;
                        sockets.add(serverSocket);
                    }
                    Socket socket = serverSocket.accept();
                    socket.setSoTimeout(TIMEOUT);
                    socket.setKeepAlive(true);
                    executor.execute(() -> server.readSocket(socket));
                } catch (IOException e) {
                    if (!closed.get()) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        });

        return server;
    }

    @Override
    public void update(UUID player, String preferred) {
        synchronized (delegate) {
            delegate.update(player, preferred);
        }
    }

    @Override
    public List<String> get(UUID player) {
        synchronized (delegate) {
            return delegate.get(player);
        }
    }

    public void get(Socket socket, UUID player) throws IOException {
        SelectResponse response = new SelectResponse(get(player));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF(gson.get().toJson(response));
        out.flush();
    }

    private void readSocket(Socket s) {
        try (Socket socket = s) {
            while (socket.isConnected()) {
                DataInputStream in = new DataInputStream(socket.getInputStream());
                int id = in.readInt();
                String contents = in.readUTF();
                if (id == 1) {
                    // select
                    SelectRequest request = gson.get().fromJson(contents, SelectRequest.class);
                    get(socket, request.getPlayer());
                } else if (id == 2) {
                    // update
                    UpdateRequest request = gson.get().fromJson(contents, UpdateRequest.class);
                    update(request.getPlayer(), request.getLocale());
                }
            }
        } catch (IOException e) {
            // close connection
        }
    }

    @Override
    public void close() throws IOException {
        closed = true;
        onClose.run();
    }

    public boolean isClosed() {
        return closed;
    }
}
