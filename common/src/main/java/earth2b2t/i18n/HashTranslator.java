package earth2b2t.i18n;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Properties;

/**
 * Stores keys/values using {@link HashMap} to provide translation efficiently.
 */
@RequiredArgsConstructor
public class HashTranslator implements Translator {
    private final String language;
    private final HashMap<String, String> map;

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public String getString(String key) {
        return map.get(key);
    }

    /**
     * Loads {@link HashTranslator} from specified {@link Properties}.
     *
     * @param language   language name
     * @param properties properties containing translation key/values
     * @return new {@link HashTranslator}
     */
    public static HashTranslator fromProperties(String language, Properties properties) {
        HashMap<String, String> map = new HashMap<>();
        for (String name : properties.stringPropertyNames()) {
            map.put(name, properties.getProperty(name));
        }
        return new HashTranslator(language, map);
    }
}
