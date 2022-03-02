package earth2b2t.i18n.bungeecord;

import earth2b2t.i18n.Location;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class TitleLocation implements Location {
    @Override
    public void print(UUID player, String msg) {
        ProxiedPlayer p = ProxyServer.getInstance().getPlayer(player);
        Title title = ProxyServer.getInstance()
                .createTitle()
                .title(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', msg)))
                .fadeIn(5)
                .stay(30)
                .fadeOut(5);
        if (p != null) p.sendTitle(title);
    }

    @Override
    public char getPrefix() {
        return 't';
    }
}
