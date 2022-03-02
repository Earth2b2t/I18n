package earth2b2t;

import earth2b2t.i18n.LanguageProvider;
import earth2b2t.i18n.net.LanguageProviderClient;
import earth2b2t.i18n.net.LanguageProviderServer;
import earth2b2t.i18n.net.SocketLanguageProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SocketLanguageProviderTests {

    private static final int PORT = 39643;

    @Test
    public void test() throws IOException {

        ExecutorService executorService = Executors.newWorkStealingPool(4);

        UUID uuid = UUID.randomUUID();
        List<String> answer = Collections.singletonList("ja_jp");

        LanguageProvider mock = mock(LanguageProvider.class);
        when(mock.get(uuid)).thenReturn(answer);

        try (SocketLanguageProvider provider0 = SocketLanguageProvider.create(PORT, executorService, mock);
             SocketLanguageProvider provider1 = SocketLanguageProvider.create(PORT, executorService, mock);
             SocketLanguageProvider provider2 = SocketLanguageProvider.create(PORT, executorService, mock)) {
            assertEquals(answer, provider0.get(uuid));

            assertEquals(answer, provider1.get(uuid));
            assertEquals(answer, provider1.get(uuid));

            assertEquals(answer, provider2.get(uuid));
            assertEquals(answer, provider2.get(uuid));
        }

        try (LanguageProviderServer server = LanguageProviderServer.create(PORT, executorService, mock);
             LanguageProviderClient client = new LanguageProviderClient("127.0.0.1", PORT)) {

            assertEquals(answer, server.get(uuid));
            assertEquals(answer, client.get(uuid));

        }
    }
}
