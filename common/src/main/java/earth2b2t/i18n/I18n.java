package earth2b2t.i18n;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * An interface used to print messages depending on player's languages.
 */
public interface I18n {

    /**
     * Sends a message based on player's language.
     *
     * @param uuid {@link UUID} of the player
     * @param key  translation key
     * @param args arguments used for placeholders, in order.
     */
    void print(UUID uuid, String key, Object... args);

    /* Bukkit-specific implementation */
    default void print(Player player, String key, Object... args) {
        print(player.getUniqueId(), key, args);
    }
}
