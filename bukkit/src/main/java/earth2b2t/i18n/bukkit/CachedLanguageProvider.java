package earth2b2t.i18n.bukkit;

import earth2b2t.i18n.provider.LanguageProvider;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Fetches language data asynchronously on joining the server, removes cache on leaving the server.
 */
public class CachedLanguageProvider implements LanguageProvider {

    private final Plugin plugin;
    private final LanguageProvider languageProvider;
    private final Map<UUID, List<String>> languages;

    private CachedLanguageProvider(Plugin plugin, LanguageProvider languageProvider) {
        this.plugin = plugin;
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
     * Creates instance and register its listener to {@link org.bukkit.plugin.PluginManager}.
     *
     * @param plugin           plugin with language directory in its classpath
     * @param languageProvider backend language provider
     * @return created {@link CachedLanguageProvider} instance
     */
    public static CachedLanguageProvider create(Plugin plugin, LanguageProvider languageProvider) {

        CachedLanguageProvider provider = new CachedLanguageProvider(plugin, languageProvider);

        for (Player player : Bukkit.getOnlinePlayers()) {
            provider.languages.put(player.getUniqueId(), languageProvider.get(player.getUniqueId()));
        }

        Bukkit.getPluginManager().registerEvents(new LoginListener(provider), plugin);
        return provider;
    }

    @RequiredArgsConstructor
    public static class LoginListener implements Listener {

        private final CachedLanguageProvider provider;

        @EventHandler
        public void onAsyncPlayerPreJoin(AsyncPlayerPreLoginEvent e) {
            if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
            provider.languages.put(e.getUniqueId(), provider.languageProvider.get(e.getUniqueId()));
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent e) {
            provider.languages.remove(e.getPlayer().getUniqueId());
        }

        @EventHandler
        public void onPluginDisable(PluginDisableEvent e) {
            if (provider.plugin != e.getPlugin()) return;
            HandlerList.unregisterAll(this);
        }
    }
}
