package earth2b2t.i18n;

import java.util.*;

abstract public class CommonI18n implements I18n {
    private final HashSet<Location> locations;
    private final Location defaultLocation;
    private final HashMap<Language, HashMap<String, Message>> cached = new HashMap<>();

    public CommonI18n(Collection<Location> locations, Location defaultLocation) {
        this.locations = new HashSet<>(locations);
        this.defaultLocation = defaultLocation;
    }

    abstract public Language getLanguage(UUID player);

    abstract public Language getDefaultLanguage();

    @Override
    public String plain(UUID player, String key, Object... args) {
        return resolve(player, key).plain(args);
    }

    @Override
    public String plain(String key, Object... args) {
        Language language = getDefaultLanguage();
        if (language == null) throw new NullPointerException("Default language is not set");
        return resolve(language, key).plain(args);
    }

    @Override
    public void print(UUID player, String key, Object... args) {
        resolve(player, key).print(player, args);
    }

    private Message resolve(Language language, String key) {
        HashMap<String, Message> lang = cached.computeIfAbsent(language, k -> new HashMap<>());
        Message message = lang.get(key);
        if (message == null) {
            String str = language.getString(key);
            if (str == null) throw new IllegalArgumentException("Unknown translation key: " + key);
            message = compile(str);
            lang.put(key, message);
        }
        return message;
    }

    private Message resolve(UUID player, String key) {
        Language language = getLanguage(player);
        Language defaultLanguage = getDefaultLanguage();
        if (language == null && defaultLanguage == null) {
            throw new NullPointerException("Default language is not set");
        } else if (defaultLanguage != null) {
            language = new MergedLanguage(language, defaultLanguage);
        }
        return resolve(language, key);
    }

    public Message compile(String key) {
        ArrayList<ArrayList<String>> messages = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();
        ArrayList<Location> locations = new ArrayList<>();
        messages.add(new ArrayList<>());
        locations.add(defaultLocation);

        boolean escaping = false;
        StringBuilder builder = new StringBuilder();

        int len = key.codePointCount(0, key.length());
        for (int i = 0; i < len; i++) {
            int code = key.codePointAt(i);
            if (escaping) {
                if (code == '\\' || code == '#' || code == '{') {
                    escaping = false;
                    builder.appendCodePoint(code);
                    continue;
                } else {
                    throw new IllegalArgumentException("Illegal escape character for " + key);
                }
            }

            if (code == '\\') {
                escaping = true;
            } else if (code == '#') {
                if (i + 1 >= len) {
                    throw new IllegalArgumentException("Illegal location character position for " + key);
                }
                int prefix = key.codePointAt(i + 1);
                boolean found = false;
                for (Location location : this.locations) {
                    if (prefix == location.getPrefix()) {
                        messages.get(messages.size() - 1).add(builder.toString());
                        builder.delete(0, builder.length());
                        messages.add(new ArrayList<>());
                        locations.add(location);
                        found = true;
                        break;
                    }
                }
                if (!found) throw new IllegalArgumentException("Unknown location prefix for " + key);
                i++;
            } else if (code == '{') {
                StringBuilder strIndex = new StringBuilder();
                while (key.codePointAt(++i) != '}') {
                    strIndex.appendCodePoint(key.codePointAt(i));
                }
                try {
                    indexes.add(Integer.parseInt(strIndex.toString()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Illegal index number for " + key, e);
                }
                messages.get(messages.size() - 1).add(builder.toString());
                builder.delete(0, builder.length());
            } else {
                builder.appendCodePoint(code);
            }
        }
        messages.get(messages.size() - 1).add(builder.toString());
        return new Message(messages.stream().map(it -> it.toArray(new String[0])).toArray(String[][]::new),
                indexes.stream().mapToInt(it -> it).toArray(), locations.toArray(new Location[0]));
    }

    public static class Message {
        private final String[][] messages;
        private final int[] indexes;
        private final Location[] locations;

        public Message(String[][] messages, int[] indexes, Location[] locations) {
            this.messages = messages;
            this.indexes = indexes;
            this.locations = locations;
        }

        private String toString(int index, Object... args) {
            int converted = indexes[index++];
            if (converted >= args.length) {
                return "{" + index + "}";
            } else {
                return args[converted].toString();
            }
        }

        public String plain(Object... args) {
            StringBuilder builder = new StringBuilder();
            int index = 0;
            for (String[] message : messages) {
                for (int j = 0; j < message.length; j++) {
                    builder.append(message[j]);
                    if (j == message.length - 1) continue;
                    builder.append(toString(index++, args));
                }
            }
            return builder.toString();
        }

        public void print(UUID player, Object... args) {
            int index = 0;
            for (int i = 0; i < messages.length; i++) {
                StringBuilder builder = new StringBuilder();
                for (int j = 0; j < messages[i].length; j++) {
                    builder.append(messages[i][j]);
                    if (j == messages[i].length - 1) continue;
                    builder.append(toString(index++, args));
                }
                locations[i].print(player, builder.toString());
            }
        }
    }
}
