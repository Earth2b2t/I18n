package earth2b2t.bukkit;

import earth2b2t.i18n.LanguageProvider;
import earth2b2t.i18n.RemoteLanguageProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class CachedLanguageProvider implements LanguageProvider {

    private final Plugin plugin;
    private final RemoteLanguageProvider languageProvider;
    private final LanguageProvider fallback;
    private final Map<UUID, List<String>> locales;

    public CachedLanguageProvider(Plugin plugin, RemoteLanguageProvider languageProvider, LanguageProvider fallback) {
        this.plugin = plugin;
        this.languageProvider = languageProvider;
        this.fallback = fallback;
        this.locales = Collections.synchronizedMap(new WeakHashMap<>());
    }

    public static CachedLanguageProvider create(Plugin plugin, RemoteLanguageProvider remoteLanguageProvider) {

        OptionLanguageProvider optionLanguageProvider = new OptionLanguageProvider();
        CachedLanguageProvider provider = new CachedLanguageProvider(plugin, remoteLanguageProvider, optionLanguageProvider);

        Bukkit.getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onAsyncPlayerPreJoin(AsyncPlayerPreLoginEvent e) {
                provider.putLocale(e.getUniqueId(), remoteLanguageProvider.get(e.getUniqueId()));
            }

            @EventHandler
            public void onPluginDisable(PluginDisableEvent e) {
                if (plugin != e.getPlugin()) return;
                HandlerList.unregisterAll(this);
            }

        }, plugin);

        return provider;
    }

    public void putLocale(UUID uuid, List<String> locale) {
        locales.put(uuid, new ArrayList<>(locale));
    }

    @Override
    public void update(UUID player, String preferred) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> languageProvider.update(player, preferred));
    }

    @Override
    public List<String> get(UUID player) {
        List<String> result = locales.get(player);
        if (result == null || result.isEmpty()) result = fallback.get(player);
        return result;
    }
}
