package earth2b2t.i18n;

import earth2b2t.i18n.bukkit.CachedLanguageProvider;
import earth2b2t.i18n.bukkit.ChatLocation;
import earth2b2t.i18n.bukkit.OptionLanguageProvider;
import earth2b2t.i18n.bukkit.SubTitleLocation;
import earth2b2t.i18n.bukkit.TitleLocation;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.WeakHashMap;

public class BukkitI18n extends PropertiesI18n {

    /* This has to be WeakHashMap, as memory-leak issues might arise on plugin unloading */
    private static final WeakHashMap<Plugin, BukkitI18n> cached = new WeakHashMap<>();
    private static final Collection<Location> LOCATIONS;
    private static final Location DEFAULT_LOCATION = new ChatLocation();

    static {
        LOCATIONS = Collections.unmodifiableCollection(Arrays.asList(
                DEFAULT_LOCATION, new TitleLocation(), new SubTitleLocation()
        ));
    }

    private final Plugin plugin;

    private BukkitI18n(Plugin plugin) throws IOException {
        super(plugin.getDataFolder(), plugin.getClass(), LOCATIONS, DEFAULT_LOCATION);
        this.plugin = plugin;
    }

    public static BukkitI18n get(Class<?> c) {
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(c);
        if (plugin == null) {
            throw new IllegalArgumentException("Provided class is not a part of any plugin: " + c.getCanonicalName());
        }
        return get(plugin);
    }

    public static BukkitI18n get(Plugin plugin) {
        BukkitI18n i18n = cached.get(plugin);
        if (i18n != null) return i18n;
        try {
            i18n = new BukkitI18n(plugin);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        cached.put(plugin, i18n);
        return i18n;
    }

    @Override
    public LanguageProvider newLanguageProvider() {
        return new CachedLanguageProvider(plugin,
                new RemoteLanguageProviderAdapter(new FileLanguageProvider(plugin.getDataFolder().toPath().resolve("lang/players"))),
                new OptionLanguageProvider());
    }
}
