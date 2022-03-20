package earth2b2t;

import earth2b2t.i18n.HashTranslator;
import earth2b2t.i18n.Translator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CommonI18nTests {

    @Test
    public void testPrint() {
        HashMap<String, String> map = new HashMap<>();
        map.put("test1", "{2} {0} {1}");
        map.put("test2", "\\{0}{1}");
        map.put("test3", "\\\\{0}");
        Translator translator = new HashTranslator("ja_JP", map);
        LocationMock mock = new LocationMock('c');
        SingleI18n i18n = new SingleI18n(translator, mock);

        i18n.print(null, "test1", 0, "1", 2);
        assertEquals("2 0 1", i18n.plain((UUID) null, "test1", 0, "1", 2));
        assertEquals("2 0 1", mock.getMessage());

        i18n.print(null, "test2", null, "1");
        assertEquals("{0}1", i18n.plain((UUID) null, "test2", null, "1"));
        assertEquals("{0}1", mock.getMessage());

        i18n.print(null, "test3", "0");
        assertEquals("\\0", i18n.plain((UUID) null, "test3", "0"));
        assertEquals("\\0", mock.getMessage());
    }
}
