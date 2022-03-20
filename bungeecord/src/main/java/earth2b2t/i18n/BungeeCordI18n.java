package earth2b2t.i18n;

import earth2b2t.i18n.bungeecord.CachedLanguageProvider;
import earth2b2t.i18n.bungeecord.location.ActionLocation;
import earth2b2t.i18n.bungeecord.location.ChatLocation;
import earth2b2t.i18n.bungeecord.location.SubTitleLocation;
import earth2b2t.i18n.bungeecord.location.TitleLocation;
import earth2b2t.i18n.provider.FileLanguageProvider;
import earth2b2t.i18n.provider.LanguageProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;

/**
 * An I18n implementation for BungeeCord API.
 * This class will cache its data on joining/leaving the server, and provides better efficiency.
 */
public class BungeeCordI18n extends ClasspathI18n {

    /* This has to be WeakHashMap, as memory-leak issues might arise on plugin unloading */
    private static final WeakHashMap<Plugin, BungeeCordI18n> cached = new WeakHashMap<>();
    private final Plugin plugin;

    private BungeeCordI18n(Plugin plugin) throws IOException {
        super(plugin.getDataFolder().toPath().resolve("lang"), plugin.getClass().getClassLoader());
        this.plugin = plugin;
    }

    @Override
    public LanguageProvider getLanguageProvider() {
        LanguageProvider languageProvider = super.getLanguageProvider();
        if (languageProvider == null) {
            Path path = plugin.getDataFolder().toPath().resolve("lang/players");
            languageProvider = CachedLanguageProvider.create(plugin, new FileLanguageProvider(plugin.getDataFolder().toPath().resolve(path)));
            super.setLanguageProvider(languageProvider);
        }
        return languageProvider;
    }

    @Override
    public Collection<Location> getLocations() {
        Collection<Location> locations = super.getLocations();
        if (locations == null) {
            locations = List.of(getDefaultLocation(), new TitleLocation(), new SubTitleLocation(), new ActionLocation());
            super.setLocations(locations);
        }
        return locations;
    }

    @Override
    public Location getDefaultLocation() {
        Location defaultLocation = super.getDefaultLocation();
        if (defaultLocation == null) {
            defaultLocation = new ChatLocation();
            super.setDefaultLocation(defaultLocation);
        }
        return defaultLocation;
    }

    /**
     * Gets a message based on player's language.
     *
     * @param sender target message sender. If the sender is {@link ProxiedPlayer}, player specific language will be used
     * @param key    translation key
     * @param args   arguments used for placeholders, in order.
     * @return message based on player's language
     */
    public String plain(CommandSender sender, String key, Object... args) {
        if (sender instanceof ProxiedPlayer) {
            return plain(((ProxiedPlayer) sender).getUniqueId(), key, args);
        } else {
            return plain(key, args);
        }
    }

    /**
     * Sends a message based on player's language.
     *
     * @param sender target message sender. If the sender is {@link ProxiedPlayer}, player specific language will be used
     * @param key    translation key
     * @param args   arguments used for placeholders, in order.
     */
    public void print(CommandSender sender, String key, Object... args) {
        if (sender instanceof ProxiedPlayer) {
            print(((ProxiedPlayer) sender).getUniqueId(), key, args);
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(plain(key, args)));
        }
    }

    /**
     * Gets {@link BungeeCordI18n} instance matching the plugin loading the specified class.
     * If {@link BungeeCordI18n#get(Plugin)} was not called before this method, a newly generated {@link BungeeCordI18n} instance will be used.
     *
     * @param c class which your plugin loaded
     * @return instance matching the plugin loading the specified class.
     */
    public static BungeeCordI18n get(Class<?> c) {
        for (Plugin plugin : ProxyServer.getInstance().getPluginManager().getPlugins()) {
            if (plugin.getClass().getClassLoader() == c.getClassLoader()) {
                return get(plugin);
            }
        }

        throw new IllegalArgumentException("Provided class is not a part of any plugin: " + c.getCanonicalName());
    }

    /**
     * Gets {@link BungeeCordI18n} matching the plugin.
     * If there was no cached {@link BungeeCordI18n} instance, this method will create a new one for the plugin.
     *
     * @param plugin plugin with language directory in its classpath
     * @return existing {@link BungeeCordI18n} instance or newly created one
     */
    public static BungeeCordI18n get(Plugin plugin) {
        BungeeCordI18n i18n = cached.get(plugin);
        if (i18n != null) return i18n;
        try {
            i18n = new BungeeCordI18n(plugin);
            i18n.getLanguageProvider();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        cached.put(plugin, i18n);
        return i18n;
    }
}
