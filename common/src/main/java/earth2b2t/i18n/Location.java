package earth2b2t.i18n;

import java.util.UUID;

public interface Location {
    void print(UUID player, String msg);
    char getPrefix();
}
