package earth2b2t.i18n.bungeecord;

import earth2b2t.i18n.LanguageProvider;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class OptionLanguageProvider implements LanguageProvider {

    @Override
    public void update(UUID player, String preferred) {
        // do nothing
    }

    @Override
    public List<String> get(UUID player) {
        ProxiedPlayer p = ProxyServer.getInstance().getPlayer(player);
        if (p != null) {
            return Collections.singletonList(p.getLocale().getLanguage());
        }

        return Collections.emptyList();
    }
}
