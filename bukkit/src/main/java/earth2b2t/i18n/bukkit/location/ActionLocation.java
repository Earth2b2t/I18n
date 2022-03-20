package earth2b2t.i18n.bukkit.location;

import earth2b2t.i18n.Location;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Sends a message in Player's action bar.
 */
public class ActionLocation implements Location {
    @Override
    public void print(UUID player, String msg) {
        Player p = Bukkit.getPlayer(player);
        if (p == null) return;
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.translateAlternateColorCodes('&', msg)));
    }

    @Override
    public char getPrefix() {
        return 'a';
    }
}
