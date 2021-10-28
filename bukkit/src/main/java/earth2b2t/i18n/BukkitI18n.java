package earth2b2t.i18n;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class BukkitI18n extends CommonI18n {

    /* This has to be WeakHashMap, as memory-leak issues might arise on plugin unloading */
    private static final WeakHashMap<Plugin, BukkitI18n> cached = new WeakHashMap<>();
    private static final Collection<Location> LOCATIONS;
    private static final Location DEFAULT_LOCATION = new ChatLocation();

    static {
        LOCATIONS = Collections.unmodifiableCollection(Arrays.asList(
                DEFAULT_LOCATION, new TitleLocation(), new SubTitleLocation()
        ));
    }

    private final ArrayList<Language> languages = new ArrayList<>();
    private Language defaultLanguage;

    /**
     * Search for missing translation keys and add them.
     *
     * @return fully updated properties
     */
    private String updateProperties(Plugin plugin, ClassLoader classLoader, String lang) throws IOException {

        // check file existence
        File currentFile = new File(plugin.getDataFolder(), lang);
        if (!currentFile.exists()) {
            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(lang), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append('\n');
                }
            }
            return builder.toString();
        }

        // load latest
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(lang), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split("=", 2);
                if (split.length < 2) continue;
                values.put(split[0], split[1]);
            }
        }

        // load current
        StringBuilder builder = new StringBuilder();
        Properties properties = new Properties();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(currentFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        }
        properties.load(new StringReader(builder.toString()));

        // update
        if (!builder.toString().endsWith("\n") && builder.length() != 0) {
            builder.append("\n");
        }

        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (properties.containsKey(entry.getKey())) continue;
            builder.append(entry.getKey());
            builder.append('=');
            builder.append(entry.getValue());
            builder.append('\n');
        }

        return builder.toString();
    }

    private BukkitI18n(Plugin plugin) throws IOException {
        super(LOCATIONS, DEFAULT_LOCATION);
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        File langDir = new File(plugin.getDataFolder(), "lang");
        langDir.mkdirs();

        URL url = classLoader.getResource("lang");
        if (url == null) {
            throw new IllegalArgumentException("The plugin doesn't have lang directory");
        }

        // hacky way to list lang directory
        ArrayList<String> entries = new ArrayList<>();
        ZipFile jarFile = new ZipFile(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        for (ZipEntry entry : Collections.list(jarFile.entries())) {
            if (entry.getName().startsWith("lang/") && entry.getName().endsWith(".properties")) {
                entries.add(entry.getName());
            }
        }

        // copy lang directory
        for (String entry : entries) {
            String properties = updateProperties(plugin, classLoader, entry);
            File propertiesFile = new File(plugin.getDataFolder(), entry);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(propertiesFile), StandardCharsets.UTF_8))) {
                writer.write(properties);
            }
        }

        // load languages
        for (File file : langDir.listFiles()) {
            Properties properties = new Properties();
            try (Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                properties.load(in);
            }
            languages.add(SingleLanguage.fromProperties(file.getName().replace(".properties", ""), properties));
        }
    }

    @Override
    public Language getLanguage(UUID player) {
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            String locale = p.getLocale();
            for (Language language : languages) {
                if (language.getLocale().equalsIgnoreCase(locale)) return language;
            }
        }
        return null;
    }

    @Override
    public String plain(String key, Object... args) {
        return ChatColor.translateAlternateColorCodes('&', super.plain(key, args));
    }

    @Override
    public String plain(UUID player, String key, Object... args) {
        return ChatColor.translateAlternateColorCodes('&', super.plain(player, key, args));
    }

    @Override
    public Language getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String lang) {
        for (Language language : languages) {
            if (language.getLocale().equals(lang)) {
                defaultLanguage = language;
                return;
            }
        }
        throw new IllegalArgumentException("Could not find the language: " + lang);
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
}
