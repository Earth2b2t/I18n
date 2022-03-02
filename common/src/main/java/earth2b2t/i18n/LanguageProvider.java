package earth2b2t.i18n;

import java.util.List;
import java.util.UUID;

public interface LanguageProvider {

    void update(UUID player, String preferred);

    List<String> get(UUID player);
}
