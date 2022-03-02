package earth2b2t.bukkit;

import earth2b2t.i18n.Location;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TitleLocation implements Location {
    @Override
    public void print(UUID player, String msg) {
        Player p = Bukkit.getPlayer(player);
        if (p != null) p.sendTitle(ChatColor.translateAlternateColorCodes('&', msg), null, 5, 30, 5);
    }

    @Override
    public char getPrefix() {
        return 't';
    }
}
