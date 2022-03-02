package earth2b2t.bukkit;

import earth2b2t.i18n.LanguageProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            return Collections.singletonList(p.getLocale());
        }

        return Collections.emptyList();
    }
}
