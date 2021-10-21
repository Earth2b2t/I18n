package earth2b2t;

import earth2b2t.i18n.Location;

import java.util.UUID;

public class LocationMock implements Location {

    private final char prefix;
    private UUID player;
    private String msg;

    public LocationMock(char prefix) {
        this.prefix = prefix;
    }

    @Override
    public void print(UUID player, String msg) {
        this.player = player;
        this.msg = msg;
    }

    @Override
    public char getPrefix() {
        return prefix;
    }

    public UUID getPlayer() {
        return player;
    }

    public String getMessage() {
        return msg;
    }
}
