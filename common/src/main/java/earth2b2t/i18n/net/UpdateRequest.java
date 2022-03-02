package earth2b2t.i18n.net;

import java.util.UUID;

public class UpdateRequest {
    private UUID player;
    private String locale;

    public UpdateRequest() {
    }

    public UpdateRequest(UUID player, String locale) {
        this.player = player;
        this.locale = locale;
    }

    public UUID getPlayer() {
        return player;
    }

    public String getLocale() {
        return locale;
    }
}
