package earth2b2t.i18n;

import org.bukkit.ChatColor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

abstract public class PropertiesI18n extends CommonI18n {

    private final ArrayList<Language> languages = new ArrayList<>();
    private final File dataFolder;
    private LanguageProvider languageProvider;
    private Language defaultLanguage;

    protected PropertiesI18n(File dataFolder, Class<?> loader, Collection<Location> locations, Location defaultLocation) throws IOException {
        super(locations, defaultLocation);
        this.dataFolder = dataFolder;
        File langDir = new File(dataFolder, "lang");
        langDir.mkdirs();

        ClassLoader classLoader = loader.getClassLoader();
        URL url = classLoader.getResource("lang");
        if (url == null) {
            throw new IllegalArgumentException("The plugin doesn't have lang directory");
        }

        // hacky way to list lang directory
        ArrayList<String> entries = new ArrayList<>();
        ZipFile jarFile = new ZipFile(loader.getProtectionDomain().getCodeSource().getLocation().getPath());
        for (ZipEntry entry : Collections.list(jarFile.entries())) {
            if (entry.getName().startsWith("lang/") && entry.getName().endsWith(".properties")) {
                entries.add(entry.getName());
            }
        }

        // copy lang directory
        for (String entry : entries) {
            String properties = updateProperties(classLoader, entry);
            File propertiesFile = new File(dataFolder, entry);
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(propertiesFile), StandardCharsets.UTF_8))) {
                writer.write(properties);
            }
        }

        // load languages
        for (File file : langDir.listFiles()) {
            if (!file.isFile()) continue;
            if (!file.getName().endsWith(".properties")) continue;
            Properties properties = new Properties();
            try (Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                properties.load(in);
            }
            languages.add(SingleLanguage.fromProperties(file.getName().replace(".properties", ""), properties));
        }
    }

    abstract public LanguageProvider newLanguageProvider();

    /**
     * Search for missing translation keys and add them.
     *
     * @return fully updated properties
     */
    private String updateProperties(ClassLoader classLoader, String lang) throws IOException {

        // check file existence
        File currentFile = new File(dataFolder, lang);
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

    @Override
    public ArrayList<Language> getLanguages() {
        return languages;
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

    @Override
    public LanguageProvider getLanguageProvider() {
        if (languageProvider == null) {
            languageProvider = newLanguageProvider();
        }
        return languageProvider;
    }
}
