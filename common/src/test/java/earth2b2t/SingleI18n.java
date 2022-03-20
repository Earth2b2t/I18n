package earth2b2t;

import earth2b2t.i18n.CommonI18n;
import earth2b2t.i18n.Location;
import earth2b2t.i18n.Translator;
import earth2b2t.i18n.provider.LanguageProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SingleI18n extends CommonI18n {

    private final Translator translator;
    private final Location location;
    private final LanguageProvider languageProvider;

    public SingleI18n(Translator translator, Location location) {
        this.translator = translator;
        this.location = location;
        this.languageProvider = new EmptyLanguageProvider();
    }

    @Override
    public Collection<Translator> getTranslators() {
        return Collections.singleton(translator);
    }

    @Override
    public Translator getDefaultTranslator() {
        return translator;
    }

    @Override
    public LanguageProvider getLanguageProvider() {
        return languageProvider;
    }

    @Override
    public Collection<Location> getLocations() {
        return List.of(location);
    }

    @Override
    public Location getDefaultLocation() {
        return location;
    }
}
