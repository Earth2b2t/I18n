package earth2b2t.i18n;

import earth2b2t.i18n.provider.LanguageProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * An {@link I18n} implementation with the ability to compile messages.
 */
abstract public class CommonI18n implements I18n {

    private final HashMap<Translator, HashMap<String, Message>> cachedMessages = new HashMap<>();

    /**
     * Gets all available {@link Translator} list.
     *
     * @return all available {@link Translator} list.
     */
    abstract public Collection<Translator> getTranslators();

    /**
     * Gets default {@link Translator} that will be used when the player doesn't have any language preference.
     *
     * @return default {@link Translator}
     */
    abstract public Translator getDefaultTranslator();

    /**
     * Gets {@link LanguageProvider} this class uses.
     *
     * @return {@link LanguageProvider} this class uses
     */
    abstract public LanguageProvider getLanguageProvider();

    /**
     * Gets all available {@link Location} list.
     *
     * @return all available {@link Location} listn
     */
    abstract public Collection<Location> getLocations();

    /**
     * Gets default {@link Location}.
     *
     * @return default {@link Location}
     */
    abstract public Location getDefaultLocation();

    @Override
    public String plain(UUID player, String key, Object... args) {
        return resolve(player, key).plain(args);
    }

    @Override
    public String plain(String key, Object... args) {
        Translator translator = getDefaultTranslator();
        if (translator == null) throw new NullPointerException("Default language is not set");
        return resolve(translator, key).plain(args);
    }

    @Override
    public void print(UUID player, String key, Object... args) {
        resolve(player, key).print(player, args);
    }

    private Message resolve(Translator translator, String key) {
        HashMap<String, Message> lang = cachedMessages.computeIfAbsent(translator, k -> new HashMap<>());
        Message message = lang.get(key);
        if (message == null) {
            String str = translator.getString(key);
            if (str == null) str = getDefaultTranslator().getString(key);
            if (str == null) throw new IllegalArgumentException("Unknown translation key: " + key);
            message = Message.compile(str, getLocations(), getDefaultLocation());
            lang.put(key, message);
        }
        return message;
    }

    private Message resolve(UUID player, String key) {
        HashMap<String, Translator> languages = new HashMap<>();
        for (Translator lang : getTranslators()) {
            languages.put(lang.getLanguage(), lang);
        }

        List<String> list = getLanguageProvider().get(player);
        Translator translator = getDefaultTranslator();
        if (list != null) {
            for (String str : list) {
                translator = languages.get(str);
                if (translator != null) break;
            }
        }

        if (translator == null) {
            throw new IllegalStateException("Default language is not set");
        }

        return resolve(translator, key);
    }
}
