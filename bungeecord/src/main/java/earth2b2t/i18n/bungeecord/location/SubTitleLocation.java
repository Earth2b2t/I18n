package earth2b2t.i18n.bungeecord.location;

import earth2b2t.i18n.Location;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * Sends a message in player's subtitle.
 */
public class SubTitleLocation implements Location {
    @Override
    public void print(UUID player, String msg) {
        ProxiedPlayer p = ProxyServer.getInstance().getPlayer(player);
        if (p == null) return;
        Title title = ProxyServer.getInstance()
                .createTitle()
                .subTitle(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', msg)))
                .fadeIn(5)
                .stay(30)
                .fadeOut(5);
        p.sendTitle(title);
    }

    @Override
    public char getPrefix() {
        return 's';
    }
}
