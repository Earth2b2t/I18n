package earth2b2t.i18n;

/**
 * Represents a language.
 */
public interface Translator {

    /**
     * Returns the language.
     * e.g. for Japanese, {@code ja_jp} should be returned.
     *
     * @return language
     */
    String getLanguage();

    /**
     * Returns the translation matching translation key.
     *
     * @param key translation key
     * @return translation
     */
    String getString(String key);
}
