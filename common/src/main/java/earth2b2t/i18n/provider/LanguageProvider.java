package earth2b2t.i18n.provider;

import java.io.Closeable;
import java.util.List;
import java.util.UUID;

/**
 * Provides players' preferred languages.
 */
public interface LanguageProvider extends Closeable {

    /**
     * Update player's the most preferred language.
     *
     * @param player    player's {@link UUID}
     * @param preferred The most preferred language
     */
    void update(UUID player, String preferred);

    /**
     * Get player's preferred language by order.
     *
     * @param player player's {@link UUID}
     * @return List of preferred languages by order
     */
    List<String> get(UUID player);
}
