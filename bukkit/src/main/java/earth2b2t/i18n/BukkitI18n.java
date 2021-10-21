package earth2b2t.i18n;

import org.bukkit.Bukkit;
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
    private Language fallback;

    private BukkitI18n(Plugin plugin) throws IOException {
        super(LOCATIONS, DEFAULT_LOCATION);
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        byte[] buffer = new byte[8192];
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
            if (entry.getName().startsWith("lang/")) entries.add(entry.getName());
        }

        // copy lang directory
        for (String entry : entries) {
            try (BufferedInputStream in = new BufferedInputStream(classLoader.getResourceAsStream(entry))) {
                File localeFile = new File(plugin.getDataFolder(), entry);
                if (localeFile.exists()) continue;
                localeFile.createNewFile();
                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(localeFile))) {
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }
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
                if (language.getLocale().equals(locale)) {
                    if (fallback == null) {
                        return language;
                    } else {
                        return new MergedLanguage(language, fallback);
                    }
                }
            }
        }
        return fallback;
    }

    public Language getFallbackLanguage() {
        return fallback;
    }

    public void setFallbackLanguage(String lang) {
        for (Language language : languages) {
            if (language.getLocale().equals(lang)) {
                fallback = language;
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
