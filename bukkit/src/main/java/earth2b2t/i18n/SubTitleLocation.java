package earth2b2t.i18n;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SubTitleLocation implements Location {
    @Override
    public void print(UUID player, String msg) {
        Player p = Bukkit.getPlayer(player);
        if (p != null) p.sendTitle(null, ChatColor.translateAlternateColorCodes('&', msg), 5, 30, 5);
    }

    @Override
    public char getPrefix() {
        return 's';
    }
}