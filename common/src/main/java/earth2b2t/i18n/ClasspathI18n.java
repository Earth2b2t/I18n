package earth2b2t.i18n;

import earth2b2t.i18n.provider.LanguageProvider;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads all languages in lang directory from specified directory.
 * This class loads all languages in lang directory from specified {@link ClassLoader} and then copies them beforehand.
 */
@Getter
@Setter
abstract public class ClasspathI18n extends CommonI18n {

    private static final char COLOR_CHAR = '\u00A7';
    private static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    private final Collection<Translator> translators;
    private final Path baseDir;
    private Translator defaultTranslator;
    private LanguageProvider languageProvider;
    private Collection<Location> locations;
    private Location defaultLocation;

    /**
     * Creates {@link ClasspathI18n} instance.
     * Be careful because this constructor automatically loads all languages from classpath and then copies to the specified directory.
     *
     * @param baseDir   directory where the language files are copied to/loaded from
     * @param mainClass main class
     * @throws IOException when an IO error occurs while loading from classpath or copying to the specified directory
     */
    protected ClasspathI18n(Path baseDir, Class<?> mainClass) throws IOException {
        this.baseDir = baseDir;
        Files.createDirectories(baseDir);

        // copy lang directory
        for (String entry : scan(mainClass)) {
            String properties = updateProperties(mainClass.getClassLoader(), "lang/" + entry);
            Files.writeString(baseDir.resolve(entry), properties);
        }

        ArrayList<Translator> translators = new ArrayList<>();
        this.translators = Collections.unmodifiableList(translators);
        // load languages
        for (Path file : Files.list(baseDir).toList()) {
            if (!Files.isReadable(file)) continue;
            if (!file.toString().endsWith(".properties")) continue;
            Properties properties = new Properties();
            properties.load(new StringReader(Files.readString(file)));
            translators.add(HashTranslator.fromProperties(file.getFileName().toString().replace(".properties", ""), properties));
        }

    }

    /**
     * Sets the default languages to {@link Translator} with corresponding {@link Translator#getLanguage()}
     *
     * @param name language name
     */
    public void setDefaultLanguage(String name) {
        this.defaultTranslator = this.getTranslators().stream()
                .filter(it -> it.getLanguage().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Language " + name + " was not found"));
    }

    @Override
    public String plain(String key, Object... args) {
        return translateAlternateColorCodes('&', super.plain(key, args));
    }

    @Override
    public String plain(UUID player, String key, Object... args) {
        return translateAlternateColorCodes('&', super.plain(player, key, args));
    }

    /**
     * Search for missing translation keys and add them.
     *
     * @return fully updated properties
     */
    private String updateProperties(ClassLoader classLoader, String lang) throws IOException {

        String classpath;
        try (InputStream in = classLoader.getResourceAsStream(lang)) {
            if (in == null) {
                throw new IOException(lang + " was not found in classpath");
            }
            classpath = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        // check file existence
        Path currentFile = baseDir.resolve(lang);
        if (!Files.exists(currentFile)) return classpath;

        // load latest
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        Arrays.stream(classpath.split("\\R"))
                .map(it -> it.split("=", 2))
                .forEachOrdered(it -> values.put(it[0], it[1]));

        // load current
        String current = Files.readString(currentFile);
        StringBuilder builder = new StringBuilder(current);
        Properties properties = new Properties();
        properties.load(new StringReader(current));

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

    private static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && ALL_CODES.indexOf(b[i + 1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    private static List<String> scan(Class<?> c) throws IOException {
        List<String> entries = new ArrayList<>();

        // Maven/Gradle
        try (InputStream in = c.getResourceAsStream("/lang")) {
            String contents = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            if (!contents.isEmpty()) {
                entries.addAll(Arrays.asList(contents.split("\\R")));
            }
        }

        // Jar
        if (entries.isEmpty()) {
            URL url = c.getProtectionDomain().getCodeSource().getLocation();
            ZipFile zipFile;

            try {
                zipFile = new ZipFile(url.toURI().getPath());
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }

            for (ZipEntry entry : Collections.list(zipFile.entries())) {
                if (entry.isDirectory()) continue;
                if (!entry.getName().startsWith("lang")) continue;
                if (!entry.getName().endsWith(".properties")) continue;
                entries.add(entry.getName().replaceFirst("lang/", ""));
            }
        }

        return entries;
    }
}
