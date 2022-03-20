package earth2b2t.i18n.bungeecord;

import earth2b2t.i18n.provider.LanguageProvider;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Fetches language data asynchronously on joining the server, removes cache on leaving the server.
 */
public class CachedLanguageProvider implements LanguageProvider, Closeable {

    private final LanguageProvider languageProvider;
    private final Map<UUID, List<String>> languages;

    private CachedLanguageProvider(LanguageProvider languageProvider) {
        this.languageProvider = languageProvider;
        this.languages = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public void update(UUID player, String preferred) {
        languageProvider.update(player, preferred);
        languages.put(player, languageProvider.get(player));
    }

    @Override
    public List<String> get(UUID player) {
        return languages.get(player);
    }

    @Override
    public void close() throws IOException {
        languageProvider.close();
    }

    /**
     * Creates instance and register its listener to {@link net.md_5.bungee.api.plugin.PluginManager}.
     *
     * @param plugin           plugin with language directory in its classpath
     * @param languageProvider backend language provider
     * @return created {@link CachedLanguageProvider} instance
     */
    public static CachedLanguageProvider create(Plugin plugin, LanguageProvider languageProvider) {

        CachedLanguageProvider provider = new CachedLanguageProvider(languageProvider);
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, new LoginListener(provider));

        return provider;
    }

    @RequiredArgsConstructor
    private static class LoginListener implements Listener {

        private final CachedLanguageProvider provider;

        @EventHandler
        public void onAsyncPlayerPreJoin(LoginEvent e) {
            UUID uuid = e.getConnection().getUniqueId();
            provider.languages.put(uuid, provider.languageProvider.get(uuid));
        }

        @EventHandler
        public void onPlayerQuit(PlayerDisconnectEvent e) {
            provider.languages.remove(e.getPlayer().getUniqueId());
        }
    }
}
