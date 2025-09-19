
package com.minkang.ultimate.titles;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TitlesFile {
    private static File file;
    private static FileConfiguration yml;
    private static Map<Integer,String> cache = new HashMap<>();

    public static void reload(Plugin plugin) {
        file = new File(plugin.getDataFolder(), "titles.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        yml = YamlConfiguration.loadConfiguration(file);
        cache.clear();
        if (yml.getConfigurationSection("titles") != null) {
            for (String k : yml.getConfigurationSection("titles").getKeys(false)) {
                cache.put(Integer.parseInt(k), yml.getString("titles."+k));
            }
        }
    }

    public static void set(int slot, String name) {
        cache.put(slot, name);
        yml.set("titles."+slot, name);
    }

    public static void remove(int slot) {
        cache.remove(slot);
        yml.set("titles."+slot, null);
    }

    public static Map<Integer,String> getAll() {
        return new HashMap<>(cache);
    }

    public static void save() {
        try { yml.save(file); } catch (IOException ignored) {}
    }
}
