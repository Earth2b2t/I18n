package earth2b2t.i18n.bukkit;

import earth2b2t.i18n.Location;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChatLocation implements Location {

    @Override
    public void print(UUID player, String msg) {
        Player p = Bukkit.getPlayer(player);
        if (p == null) return;
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    @Override
    public char getPrefix() {
        return 'c';
    }
}
