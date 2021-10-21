package earth2b2t.i18n;

public class MergedLanguage implements Language {
    private final Language language;
    private final Language fallback;

    public MergedLanguage(Language language, Language fallback) {
        this.language = language;
        this.fallback = fallback;
    }

    @Override
    public String getLocale() {
        return language.getLocale();
    }

    @Override
    public String getString(String key) {
        String str = language.getString(key);
        if (str == null) str = fallback.getString(key);
        return str;
    }
}
