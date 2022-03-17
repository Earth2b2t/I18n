package earth2b2t.i18n;

import java.io.InputStream;

public class EmptyInputStream extends InputStream {

    @Override
    public int read() {
        return -1;
    }
}
