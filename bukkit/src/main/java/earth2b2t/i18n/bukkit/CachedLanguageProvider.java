package earth2b2t.i18n.bukkit;

import earth2b2t.i18n.LanguageProvider;
import earth2b2t.i18n.RemoteLanguageProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CachedLanguageProvider implements LanguageProvider, Closeable {

    private final Plugin plugin;
    private final RemoteLanguageProvider languageProvider;
    private final LanguageProvider fallback;
    private final Map<UUID, List<String>> locales;

    public CachedLanguageProvider(Plugin plugin, RemoteLanguageProvider languageProvider, LanguageProvider fallback) {
        this.plugin = plugin;
        this.languageProvider = languageProvider;
        this.fallback = fallback;
        this.locales = Collections.synchronizedMap(new HashMap<>());
    }

    public void putLocale(UUID uuid, List<String> locale) {
        locales.put(uuid, new ArrayList<>(locale));
    }

    public void removeLocale(UUID uuid) {
        locales.remove(uuid);
    }

    @Override
    public void update(UUID player, String preferred) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            languageProvider.update(player, preferred);
            putLocale(player, languageProvider.get(player));
        });
    }

    @Override
    public List<String> get(UUID player) {
        List<String> result = locales.get(player);
        if (result == null || result.isEmpty()) result = fallback.get(player);
        return result;
    }

    @Override
    public void close() throws IOException {
        languageProvider.close();
    }

    public static CachedLanguageProvider create(Plugin plugin, RemoteLanguageProvider remoteLanguageProvider) {

        OptionLanguageProvider optionLanguageProvider = new OptionLanguageProvider();
        CachedLanguageProvider provider = new CachedLanguageProvider(plugin, remoteLanguageProvider, optionLanguageProvider);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                provider.putLocale(player.getUniqueId(), remoteLanguageProvider.get(player.getUniqueId()));
            });
        }

        Bukkit.getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onAsyncPlayerPreJoin(AsyncPlayerPreLoginEvent e) {
                provider.putLocale(e.getUniqueId(), remoteLanguageProvider.get(e.getUniqueId()));
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) {
                provider.removeLocale(e.getPlayer().getUniqueId());
            }

            @EventHandler
            public void onPluginDisable(PluginDisableEvent e) throws IOException {
                if (plugin != e.getPlugin()) return;
                HandlerList.unregisterAll(this);
                provider.close();
            }

        }, plugin);

        return provider;
    }
}
