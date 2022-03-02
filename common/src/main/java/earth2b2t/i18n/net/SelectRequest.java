package earth2b2t.i18n.net;

import java.util.UUID;

public class SelectRequest {

    private UUID player;

    public SelectRequest() {
    }

    public SelectRequest(UUID player) {
        this.player = player;
    }

    public UUID getPlayer() {
        return player;
    }

}
