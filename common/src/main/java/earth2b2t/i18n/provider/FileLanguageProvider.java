package earth2b2t.i18n.provider;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Loads/saves user's language data in a directory.
 */
public class FileLanguageProvider implements LanguageProvider {

    private static final int MAX_SIZE = 10;
    private final Path baseDir;
    private final Gson gson;

    /**
     * Creates an instance.
     *
     * @param baseDir directory which player language files are saved to
     */
    public FileLanguageProvider(Path baseDir) {
        this.baseDir = baseDir;
        this.gson = new Gson();
    }

    @Override
    public synchronized void update(UUID player, String preferred) {
        List<String> list = new ArrayList<>(get(player));
        list.add(0, preferred);
        list = list.subList(0, Math.min(list.size(), MAX_SIZE));
        list = list.stream().distinct().collect(Collectors.toList());

        try {
            Files.createDirectories(baseDir);
            Files.writeString(baseDir.resolve(player + ".json"), gson.toJson(new LanguageData(player, list)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized List<String> get(UUID player) {
        Path path = baseDir.resolve(player + ".json");
        if (Files.exists(path)) {
            try {
                return gson.fromJson(Files.readString(path), LanguageData.class).getData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void close() {
    }

    private static class LanguageData {
        private final UUID uuid;
        private final List<String> data;

        public LanguageData(UUID uuid, List<String> data) {
            this.uuid = uuid;
            this.data = data;
        }

        public UUID getUUID() {
            return uuid;
        }

        public List<String> getData() {
            return data;
        }
    }
}
