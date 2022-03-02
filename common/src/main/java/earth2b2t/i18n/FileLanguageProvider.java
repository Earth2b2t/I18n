package earth2b2t.i18n;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FileLanguageProvider implements LanguageProvider {

    private static final int MAX_SIZE = 10;
    private final Path baseDir;
    private final Gson gson;

    public FileLanguageProvider(Path baseDir) {
        this.baseDir = baseDir;
        this.gson = new Gson();
    }

    @Override
    public synchronized void update(UUID player, String preferred) {
        List<String> list = new ArrayList<>(get(player));
        list.add(0, preferred);

        String contents = gson.toJson(new LocaleData(player, list.subList(0, Math.min(list.size(), MAX_SIZE))));
        try {
            Files.createDirectories(baseDir);
            Files.write(baseDir.resolve(player + ".json"), contents.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized List<String> get(UUID player) {
        Path path = baseDir.resolve(player + ".json");
        if (Files.exists(path)) {
            try {
                return gson.fromJson(new String(Files.readAllBytes(path), StandardCharsets.UTF_8), LocaleData.class).getData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }

    private static class LocaleData {
        private UUID uuid;
        private List<String> data;

        public LocaleData(UUID uuid, List<String> data) {
            this.uuid = uuid;
            this.data = data;
        }

        public LocaleData() {
        }

        public UUID getUUID() {
            return uuid;
        }

        public List<String> getData() {
            return data;
        }
    }
}
