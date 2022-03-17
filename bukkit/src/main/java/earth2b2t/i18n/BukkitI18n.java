package earth2b2t.i18n;

import earth2b2t.i18n.bukkit.ActionLocation;
import earth2b2t.i18n.bukkit.CachedLanguageProvider;
import earth2b2t.i18n.bukkit.ChatLocation;
import earth2b2t.i18n.bukkit.SubTitleLocation;
import earth2b2t.i18n.bukkit.TitleLocation;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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
                DEFAULT_LOCATION, new TitleLocation(), new SubTitleLocation(), new ActionLocation()
        ));
    }

    private final Plugin plugin;

    private BukkitI18n(Plugin plugin) throws IOException {
        super(plugin.getDataFolder(), plugin.getClass(), LOCATIONS, DEFAULT_LOCATION);
        this.plugin = plugin;
    }

    @Override
    public LanguageProvider newLanguageProvider() {
        return CachedLanguageProvider.create(plugin,
                new RemoteLanguageProviderAdapter(new FileLanguageProvider(plugin.getDataFolder().toPath().resolve("lang/players"))));
    }

    public String plain(CommandSender sender, String key, Object... args) {
        if (sender instanceof Player) {
            return plain(((Player) sender).getUniqueId(), key, args);
        } else {
            return plain(key, args);
        }
    }

    public void print(CommandSender sender, String key, Object... args) {
        if (sender instanceof Player) {
            print(((Player) sender).getUniqueId(), key, args);
        } else {
            sender.sendMessage(plain(key, args));
        }
    }

    public static BukkitI18n get(Class<?> c) {
        Plugin plugin = null;

        try {
            plugin = JavaPlugin.getProvidingPlugin(c);
        } catch (IllegalArgumentException e) {
            // ignore
        }

        // load from loaded plugins
        if (plugin == null) {
            try (InputStream in = BukkitI18n.class.getResourceAsStream("/plugin.yml")) {
                if (in != null) {
                    try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                        YamlConfiguration conf = new YamlConfiguration();
                        conf.load(reader);
                        String name = conf.getString("name");
                        if (name == null) {
                            throw new IllegalStateException("plugin.yml was found but plugin name isn't specified");
                        }
                        plugin = Bukkit.getPluginManager().getPlugin(name);
                    }
                }
            } catch (IOException | InvalidConfigurationException e) {
                // ignore
            }
        }

        // MockBukkit
        if (plugin == null) {
            plugin = Bukkit.getPluginManager().getPlugin("mock-i18n");
        }

        if (plugin == null) {
            try {
                Class<?> mockBukkit = Class.forName("be.seeseemelk.mockbukkit.MockBukkit");
                Method createMockPlugin = mockBukkit.getMethod("createMockPlugin", String.class);
                plugin = (Plugin) createMockPlugin.invoke(null, "mock-i18n");
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                // ignore
            } catch (InvocationTargetException e) {
                throw new RuntimeException("An error occurred while creating a mock plugin", e);
            }
        }

        if (plugin == null) {
            throw new IllegalArgumentException("Could not find any plugin: " + c.getCanonicalName());
        }

        return get(plugin);
    }

    public static BukkitI18n get(Plugin plugin) {
        BukkitI18n i18n = cached.get(plugin);
        if (i18n != null) return i18n;
        try {
            i18n = new BukkitI18n(plugin);
            i18n.getLanguageProvider();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        cached.put(plugin, i18n);
        return i18n;
    }
}
