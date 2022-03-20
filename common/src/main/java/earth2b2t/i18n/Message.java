package earth2b2t.i18n;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Represents a message that can generate target {@link String} from arguments.
 * This class will typically be used with {@link Translator} to generate messages efficiently,
 * because this class compiles translation value on {@link Message#compile(String, Collection, Location)}
 * and then provides message efficiently from arguments by calling {@link Message#print(UUID, Object...)} or {@link Message#plain(Object...)}
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Message {
    private final String[][] messages;
    private final int[] indexes;
    private final Location[] locations;

    private String toString(int index, Object... args) {
        int converted = indexes[index];
        if (converted >= args.length) {
            return "{" + converted + "}";
        } else {
            return args[converted].toString();
        }
    }

    /**
     * Generate {@link String} with specified arguments.
     *
     * @param args arguments
     * @return generated {@link String}
     */
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

    /**
     * Sends generated {@link String} to the player with specified arguments.
     *
     * @param player player's {@link UUID}
     * @param args   arguments
     */
    public void print(UUID player, Object... args) {
        int index = 0;
        for (int i = 0; i < messages.length; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < messages[i].length; j++) {
                builder.append(messages[i][j]);
                if (j == messages[i].length - 1) continue;
                builder.append(toString(index++, args));
            }
            String string = builder.toString();
            if (i == 0 && messages.length > 1 && string.isEmpty()) continue;
            locations[i].print(player, string);
        }
    }

    /**
     * Compiles {@link Message} instance.
     * This method might take some time and users are advised to cache generated {@link Message} for later use.
     *
     * @param key             translation key
     * @param locations       all {@link Location} list
     * @param defaultLocation default {@link Location}
     * @return generated {@link Message} instance
     */
    public static Message compile(String key, Collection<Location> locations, Location defaultLocation) {
        ArrayList<ArrayList<String>> messageList = new ArrayList<>();
        ArrayList<Integer> indexList = new ArrayList<>();
        ArrayList<Location> locationList = new ArrayList<>();

        messageList.add(new ArrayList<>());
        locationList.add(defaultLocation);

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
                for (Location location : locations) {
                    if (prefix == location.getPrefix()) {
                        messageList.get(messageList.size() - 1).add(builder.toString());
                        builder.delete(0, builder.length());
                        messageList.add(new ArrayList<>());
                        locationList.add(location);
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
                    indexList.add(Integer.parseInt(strIndex.toString()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Illegal index number for " + key, e);
                }
                messageList.get(messageList.size() - 1).add(builder.toString());
                builder.delete(0, builder.length());
            } else {
                builder.appendCodePoint(code);
            }
        }

        messageList.get(messageList.size() - 1).add(builder.toString());

        return new Message(
                messageList.stream().map(it -> it.toArray(new String[0])).toArray(String[][]::new),
                indexList.stream().mapToInt(it -> it).toArray(),
                locationList.toArray(new Location[0])
        );
    }
}
