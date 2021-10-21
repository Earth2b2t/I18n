package earth2b2t.i18n;

import java.util.HashMap;
import java.util.Properties;

public class SingleLanguage implements Language {
    private final String locale;
    private final HashMap<String, String> map;

    public SingleLanguage(String locale, HashMap<String, String> map) {
        this.locale = locale;
        this.map = map;
    }

    @Override
    public String getLocale() {
        return locale;
    }

    @Override
    public String getString(String key) {
        return map.get(key);
    }

    public static Language fromProperties(String locale, Properties properties) {
        HashMap<String, String> map = new HashMap<>();
        for (String name : properties.stringPropertyNames()) {
            map.put(name, properties.getProperty(name));
        }
        return new SingleLanguage(locale, map);
    }
}
