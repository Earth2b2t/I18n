package earth2b2t.i18n;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface I18n {
    void print(UUID uuid, String key, Object... args);

    /* Bukkit-specific implementation */
    default void print(Player player, String key, Object... args) {
        print(player.getUniqueId(), key, args);
    }
}
