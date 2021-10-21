package earth2b2t.i18n;

/**
 * Represents a language.
 */
public interface Language {

    /**
     * Returns the locale.
     * e.g. for Japanese, {@code ja_jp} should be returned.
     *
     * @return locale.
     */
    String getLocale();

    /**
     * Returns the translation matching translation key.
     *
     * @param key translation key
     * @return translation
     */
    String getString(String key);
}
