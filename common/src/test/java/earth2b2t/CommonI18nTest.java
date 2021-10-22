package earth2b2t;

import earth2b2t.i18n.Language;
import earth2b2t.i18n.SingleLanguage;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonI18nTest {

    @Test
    public void testPrint() {
        HashMap<String, String> map = new HashMap<>();
        map.put("test1", "{2} {0} {1}");
        map.put("test2", "\\{0}{1}");
        map.put("test3", "\\\\{0}");
        Language language = new SingleLanguage("ja_JP", map);
        LocationMock mock = new LocationMock('c');
        SingleI18n i18n = new SingleI18n(language, mock);

        i18n.print((UUID) null, "test1", 0, "1", 2);
        assertEquals("2 0 1", i18n.plain((UUID) null, "test1", 0, "1", 2));
        assertEquals("2 0 1", mock.getMessage());

        i18n.print((UUID) null, "test2", null, "1");
        assertEquals("{0}1", i18n.plain((UUID) null, "test2", null, "1"));
        assertEquals("{0}1", mock.getMessage());

        i18n.print((UUID) null, "test3", "0");
        assertEquals("\\0", i18n.plain((UUID) null, "test3", "0"));
        assertEquals("\\0", mock.getMessage());
    }
}
