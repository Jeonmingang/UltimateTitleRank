
package com.minkang.ultimate.titles;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class YamlFiles {
    private static File messagesFile;
    private static FileConfiguration messages;

    public static FileConfiguration getMessages(JavaPlugin plugin) {
        if (messages == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            messages = YamlConfiguration.loadConfiguration(messagesFile);
        }
        return messages;
    }

    public static void saveMessages() {
        if (messages != null && messagesFile != null) {
            try { messages.save(messagesFile); } catch (IOException ignored) {}
        }
    }
}
