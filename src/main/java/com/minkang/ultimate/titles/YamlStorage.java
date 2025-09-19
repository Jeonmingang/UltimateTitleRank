
package com.minkang.ultimate.titles;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class YamlStorage implements Storage {
    private final Plugin plugin;
    private final File dir;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public YamlStorage(Plugin plugin) {
        this.plugin = plugin;
        this.dir = new File(plugin.getDataFolder(), "data");
        if (!dir.exists()) dir.mkdirs();
    }

    @Override
    public PlayerData get(UUID uuid) {
        if (cache.containsKey(uuid)) {
            ensureToday(cache.get(uuid));
            return cache.get(uuid);
        }
        File f = new File(dir, uuid.toString() + ".yml");
        PlayerData d = new PlayerData();
        if (f.exists()) {
            FileConfiguration y = YamlConfiguration.loadConfiguration(f);
            d.setRankId(y.getString("rank", null));
            if (y.getConfigurationSection("fixed") != null) {
                for (String k : y.getConfigurationSection("fixed").getKeys(false)) {
                    d.getFixedTitles().put(Integer.parseInt(k), y.getString("fixed." + k));
                }
            }
            if (y.getConfigurationSection("custom") != null) {
                for (String k : y.getConfigurationSection("custom").getKeys(false)) {
                    d.getCustomTitles().put(Integer.parseInt(k), y.getString("custom." + k));
                }
            }
            d.setDailyLegendaryUsed(y.getInt("limits.legendary_used", 0));
            d.setDailyMegabossUsed(y.getInt("limits.megaboss_used", 0));
            if (y.getConfigurationSection("titles.custom") != null) {
                for (String k : y.getConfigurationSection("titles.custom").getKeys(false)) {
                    try { d.getCustomTitles().put(Integer.parseInt(k), y.getString("titles.custom."+k, "")); } catch (Exception ignored) {}
                }
            }
            if (y.contains("titles.active_slot")) { d.setActiveTitleSlot(y.getInt("titles.active_slot")); }
            d.setLastRankGroup(y.getString("rank.last_group", null));

            if (y.getStringList("colorchat.owned") != null) { for (String id : y.getStringList("colorchat.owned")) d.getColorsOwned().add(id); }
            if (y.getString("colorchat.active") != null) d.setActiveColorId(y.getString("colorchat.active"));
        }
        cache.put(uuid, d);
        ensureToday(d);
        return d;
    }

    private void ensureToday(PlayerData d) {
        String tz = plugin.getConfig().getString("storage.timezone", "Asia/Seoul");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(tz));
        String today = now.toLocalDate().toString();
        d.ensureToday(today);
    }

    @Override
    public void save(UUID uuid, PlayerData data) {
        cache.put(uuid, data);
        File f = new File(dir, uuid.toString() + ".yml");
        FileConfiguration y = new YamlConfiguration();
        y.set("rank", data.getRankId());
        for (Map.Entry<Integer, String> e : data.getFixedTitles().entrySet()) {
            y.set("fixed." + e.getKey(), e.getValue());
        }
        for (Map.Entry<Integer, String> e : data.getCustomTitles().entrySet()) {
            y.set("custom." + e.getKey(), e.getValue());
        }
        y.set("limits.legendary_used", data.getDailyLegendaryUsed());
        y.set("limits.megaboss_used", data.getDailyMegabossUsed());
        y.set("colorchat.owned", new java.util.ArrayList<>(data.getColorsOwned()));
        y.set("colorchat.active", data.getActiveColorId());
        try { y.save(f); } catch (IOException ignored) {}
    }

    @Override
    public void flush() { }

public int purgeSlotAllPlayers(int slot) {
    int removed = 0;
    java.io.File dir = new java.io.File(plugin.getDataFolder(), "data");
    if (!dir.exists() || dir.listFiles() == null) return 0;
    for (java.io.File f : dir.listFiles()) {
        try {
            java.util.UUID uuid = java.util.UUID.fromString(f.getName().replace(".yml",""));
            PlayerData d = get(uuid);
            if (d.getCustomTitles().remove(slot) != null) {
                removed++;
                save(uuid, d);
            }
        } catch (Exception ignored) {}
    }
    return removed;
}
}
