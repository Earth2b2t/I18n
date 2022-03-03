package earth2b2t.i18n.bungeecord;

import earth2b2t.i18n.LanguageProvider;
import earth2b2t.i18n.RemoteLanguageProvider;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            languageProvider.update(player, preferred);
            putLocale(player, languageProvider.get(player));
        }, 0, TimeUnit.MILLISECONDS);
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

        ProxyServer.getInstance().getPluginManager().registerListener(plugin, new Listener() {

            @EventHandler
            public void onAsyncPlayerPreJoin(LoginEvent e) {
                UUID uuid = e.getConnection().getUniqueId();
                provider.putLocale(uuid, remoteLanguageProvider.get(uuid));
            }

            @EventHandler
            public void onPlayerQuit(PlayerDisconnectEvent e) {
                provider.removeLocale(e.getPlayer().getUniqueId());
            }
        });

        return provider;
    }
}
