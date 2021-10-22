# I18n

Internationalization library for Minecraft.

## Why should you use this library?

Because this library is simple to use and well documented. You can also use indexed arguments and our location system to
heavily customize the output.

## Gradle

build.gradle

**Make sure to include this library in your jar file!(Use gradle shadow plugin)**

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Earth2b2t.I18n:i18n-bukkit:1.1.0'
}
```

## Tutorial(Bukkit)

1. Create lang/XXX.properties in your resource directory (XXX can be ja_jp, en_us or whatever). These files are
   automatically copied to your plugin data directory and can be customized with any text editor.
2. Create I18n instance with BukkitI18n#get(plugin), and everything is now set up!

## Example(Bukkit)

Main.java

```java
package earth2b2t.i18n.test;

import earth2b2t.i18n.BukkitI18n;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        BukkitI18n i18n = BukkitI18n.get(Main.class);
        i18n.setFallbackLanguage("ja_jp");
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
    }
}

```

JoinListener.java

```java
package earth2b2t.i18n.test;

import earth2b2t.i18n.BukkitI18n;
import earth2b2t.i18n.I18n;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final I18n i18n = BukkitI18n.get(JoinListener.class);

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        i18n.print(e.getPlayer(), "welcome", e.getPlayer().getName());
    }
}
```

ja_jp.properties

```properties
welcome=#tようこそ {0} 様!#cルールを確認するようお願いします!
```

Result

![image](https://user-images.githubusercontent.com/26406334/138262372-c2b38f15-d5e0-4928-9bc4-fefeaee5fc1f.png)
