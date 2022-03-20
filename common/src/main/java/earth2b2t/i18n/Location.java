package earth2b2t.i18n;

import java.util.UUID;

/**
 * Represents a way to send a message.
 */
public interface Location {

    /**
     * Sends a message to the player.
     *
     * @param player {@link UUID} of the player
     * @param msg    message
     */
    void print(UUID player, String msg);

    /**
     * Returns the prefix of the location.
     * This will be used to identify the location in the process of translation.
     *
     * @return prefix of the location
     */
    char getPrefix();
}
