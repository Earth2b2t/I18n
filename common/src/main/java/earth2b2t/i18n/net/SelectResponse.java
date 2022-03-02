package earth2b2t.i18n.net;

import java.util.List;

public class SelectResponse {

    private List<String> locales;

    public SelectResponse() {
    }

    public SelectResponse(List<String> locales) {
        this.locales = locales;
    }

    public List<String> getLocales() {
        return locales;
    }
}
