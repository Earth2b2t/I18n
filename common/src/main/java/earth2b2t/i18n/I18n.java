package earth2b2t.i18n;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
     * @return Message based on player's language
     */
    String plain(UUID uuid, String key, Object... args);

    /**
     * Returns a message in default language
     * Location system will be disabled with this method.
     *
     * @param key  translation key
     * @param args arguments used for placeholders, in order.
     * @return Message based on player's language
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

    /* Bukkit-specific implementation */
    default String plain(CommandSender sender, String key, Object... args) {
        if (sender instanceof Player) {
            return plain(((Player) sender).getUniqueId(), key, args);
        } else {
            return plain(key, args);
        }
    }

    /* Bukkit-specific implementation */
    default void print(CommandSender sender, String key, Object... args) {
        if (sender instanceof Player) {
            print(((Player) sender).getUniqueId(), key, args);
        } else {
            sender.sendMessage(plain(key, args));
        }
    }
}
