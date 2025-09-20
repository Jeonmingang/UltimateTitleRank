package com.minkang.ultimate.titles.storage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Storage {
    private final Plugin plugin;
    private final File file;
    private final YamlConfiguration yaml;
    private String lastResetDate;
    private final Map<UUID, PlayerData> cache = new HashMap<UUID, PlayerData>();

    public Storage(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        this.yaml = YamlConfiguration.loadConfiguration(file);
        this.lastResetDate = yaml.getString("meta.last_reset", "1970-01-01");

        for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
            get(p.getUniqueId());
        }
    }

    public synchronized void save() {
        try {
            for (Map.Entry<UUID, PlayerData> e : cache.entrySet()) {
                UUID uuid = e.getKey();
                PlayerData pd = e.getValue();
                String base = "players." + uuid.toString();
                yaml.set(base + ".minutes_played", pd.getMinutesPlayed());
                yaml.set(base + ".current_rank_id", pd.getCurrentRankId());
                yaml.set(base + ".selected_title", pd.getSelectedTitle());
                yaml.set(base + ".owned_titles", pd.getOwnedTitles());
                yaml.set(base + ".color_permissions", pd.getColorPermissions());
                yaml.set(base + ".selected_color", pd.getSelectedColor());
                yaml.set(base + ".legendary_used_today", pd.getLegendaryUsedToday());
                yaml.set(base + ".megaboss_used_today", pd.getMegabossUsedToday());
            }
            yaml.set("meta.last_reset", this.lastResetDate);
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to save players.yml: " + ex.getMessage());
        }
    }

    public synchronized PlayerData get(UUID uuid) {
        PlayerData pd = cache.get(uuid);
        if (pd != null) return pd;
        String base = "players." + uuid.toString();
        int minutes = yaml.getInt(base + ".minutes_played", 0);
        String rid = yaml.getString(base + ".current_rank_id", "");
        String selectedTitle = yaml.getString(base + ".selected_title", "");
        java.util.List<String> owned = yaml.getStringList(base + ".owned_titles");
        java.util.List<String> colors = yaml.getStringList(base + ".color_permissions");
        String selectedColor = yaml.getString(base + ".selected_color", "WHITE");
        int leg = yaml.getInt(base + ".legendary_used_today", 0);
        int mega = yaml.getInt(base + ".megaboss_used_today", 0);
        pd = new PlayerData(minutes, rid, selectedTitle, owned, colors, selectedColor, leg, mega);
        cache.put(uuid, pd);
        return pd;
    }

    public String getLastResetDate() {
        return lastResetDate;
    }

    public void resetDailyCounters(String today) {
        this.lastResetDate = today;
        for (PlayerData pd : cache.values()) {
            pd.setLegendaryUsedToday(0);
            pd.setMegabossUsedToday(0);
        }
        save();
    }
}
