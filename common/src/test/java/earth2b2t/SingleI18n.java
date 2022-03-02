package earth2b2t;

import earth2b2t.i18n.CommonI18n;
import earth2b2t.i18n.EmptyLanguageProvider;
import earth2b2t.i18n.Language;
import earth2b2t.i18n.LanguageProvider;
import earth2b2t.i18n.Location;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class SingleI18n extends CommonI18n {

    private final Language language;

    public SingleI18n(Language language, Location... location) {
        super(Arrays.asList(location), location[0]);
        this.language = language;
    }

    @Override
    public Collection<Language> getLanguages() {
        return Collections.singleton(language);
    }

    @Override
    public Language getDefaultLanguage() {
        return language;
    }

    @Override
    public LanguageProvider getLanguageProvider() {
        return new EmptyLanguageProvider();
    }
}
