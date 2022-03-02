package earth2b2t;

import com.google.common.jimfs.Jimfs;
import earth2b2t.i18n.FileLanguageProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileLanguageProviderTests {

    @Test
    public void testAll() throws IOException {
        UUID uuid = UUID.randomUUID();
        try (FileSystem fs = Jimfs.newFileSystem()) {
            FileLanguageProvider provider = new FileLanguageProvider(fs.getPath("data"));
            provider.update(uuid, "ja_jp");
            assertEquals(Collections.singletonList("ja_jp"), provider.get(uuid));
            assertEquals(Collections.emptyList(), provider.get(UUID.randomUUID()));
        }
    }
}
