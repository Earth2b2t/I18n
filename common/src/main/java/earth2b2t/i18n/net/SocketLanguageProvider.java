package earth2b2t.i18n.net;

import earth2b2t.i18n.LanguageProvider;
import earth2b2t.i18n.RemoteLanguageProvider;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

public class SocketLanguageProvider implements RemoteLanguageProvider {

    private static final int PORT = 39643;
    private final LanguageProvider languageProvider;

    public SocketLanguageProvider(LanguageProvider languageProvider) {
        this.languageProvider = languageProvider;
    }

    public static SocketLanguageProvider create(int port, Executor executor, LanguageProvider fallback) {
        LanguageProvider provider;
        try {
            provider = LanguageProviderServer.create(port, executor, fallback);
        } catch (IOException e) {
            provider = new LanguageProviderClient("127.0.0.1", port);
        }

        return new SocketLanguageProvider(provider);
    }

    public static SocketLanguageProvider create(Executor executor, LanguageProvider fallback) {
        return create(PORT, executor, fallback);
    }

    @Override
    public void update(UUID player, String preferred) {
        languageProvider.update(player, preferred);
    }

    @Override
    public List<String> get(UUID player) {
        return languageProvider.get(player);
    }

    @Override
    public void close() throws IOException {
        ((Closeable) languageProvider).close();
    }
}
