package earth2b2t;

import earth2b2t.i18n.CommonI18n;
import earth2b2t.i18n.Language;
import earth2b2t.i18n.Location;

import java.util.Arrays;
import java.util.UUID;

public class SingleI18n extends CommonI18n {

    private final Language language;

    public SingleI18n(Language language, Location... location) {
        super(Arrays.asList(location), location[0]);
        this.language = language;
    }

    @Override
    public Language getLanguage(UUID player) {
        return language;
    }

    @Override
    public Language getDefaultLanguage() {
        return language;
    }

}
