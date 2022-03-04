package earth2b2t.i18n.bungeecord;

import earth2b2t.i18n.Location;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class ChatLocation implements Location {

    @Override
    public void print(UUID player, String msg) {
        ProxiedPlayer p = ProxyServer.getInstance().getPlayer(player);
        if (p == null) return;
        p.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', msg)));
    }

    @Override
    public char getPrefix() {
        return 'c';
    }
}
