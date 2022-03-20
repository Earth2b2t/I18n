package earth2b2t.i18n;

import java.util.UUID;

/**
 * An interface used to print messages depending on player's languages.
 */
public interface I18n {

    /**
     * Returns a message based on player's language.
     * Location system will be disabled with this method.
     *
     * @param uuid {@link UUID} of the player
     * @param key  translation key
     * @param args arguments used for placeholders, in order.
     * @return message based on player's language
     */
    String plain(UUID uuid, String key, Object... args);

    /**
     * Returns a message in default language
     * Location system will be disabled with this method.
     *
     * @param key  translation key
     * @param args arguments used for placeholders, in order.
     * @return message based on player's language
     */
    String plain(String key, Object... args);

    /**
     * Sends a message based on player's language.
     *
     * @param uuid {@link UUID} of the player
     * @param key  translation key
     * @param args arguments used for placeholders, in order.
     */
    void print(UUID uuid, String key, Object... args);
}
