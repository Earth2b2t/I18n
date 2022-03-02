package earth2b2t.i18n;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class RemoteLanguageProviderAdapter implements RemoteLanguageProvider {

    private final LanguageProvider languageProvider;

    public RemoteLanguageProviderAdapter(LanguageProvider languageProvider) {
        this.languageProvider = languageProvider;
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
        if (languageProvider instanceof Closeable) {
            ((Closeable) languageProvider).close();
        }
    }
}
