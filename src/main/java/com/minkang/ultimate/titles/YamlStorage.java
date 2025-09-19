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
    private final Map<UUID, PlayerData> cache = new HashMap<>();
    private final File dir;

    public YamlStorage(Plugin plugin) {
        this.plugin = plugin;
        this.dir = new File(plugin.getDataFolder(), "data");
        this.dir.mkdirs();
    }

    @Override
    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, u -> load(u));
    }

    @Override
    public void save(UUID uuid, PlayerData data) {
        cache.put(uuid, data);
        saveFile(uuid, data);
    }

    @Override
    public int purgeSlotAllPlayers(int slot) {
        int removed = 0;
        if (!dir.exists() || dir.listFiles() == null) return 0;
        for (File f : dir.listFiles()) {
            try {
                UUID u = UUID.fromString(f.getName().replace(".yml", ""));
                PlayerData d = get(u);
                if (d.getCustomTitles().remove(slot) != null) {
                    removed++;
                    save(u, d);
                }
            } catch (Exception ignored) {}
        }
        return removed;
    }

    private PlayerData load(UUID uuid) {
        File f = new File(dir, uuid.toString()+".yml");
        FileConfiguration y = YamlConfiguration.loadConfiguration(f);
        PlayerData d = new PlayerData();
        d.setRankId(y.getString("rank.id", null));
        d.setLastRankGroup(y.getString("rank.last_group", null));
        if (y.getConfigurationSection("titles.custom") != null) {
            for (String k : y.getConfigurationSection("titles.custom").getKeys(false)) {
                try { d.getCustomTitles().put(Integer.parseInt(k), y.getString("titles.custom."+k, "")); } catch (Exception ignored) {}
            }
        }
        if (y.contains("titles.active_slot")) d.setActiveTitleSlot(y.getInt("titles.active_slot"));
        d.setDailyLegendaryUsed(y.getInt("limits.legendary_used", 0));
        d.setDailyMegabossUsed(y.getInt("limits.megaboss_used", 0));
        String today = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDate().toString();
        d.ensureToday(today);
        return d;
    }

    private void saveFile(UUID uuid, PlayerData data) {
        File f = new File(dir, uuid.toString()+".yml");
        FileConfiguration y = YamlConfiguration.loadConfiguration(f);
        y.set("rank.id", data.getRankId());
        y.set("rank.last_group", data.getLastRankGroup());
        Map<String,String> tmap = new HashMap<>();
        for (Map.Entry<Integer,String> e : data.getCustomTitles().entrySet()) tmap.put(String.valueOf(e.getKey()), e.getValue());
        y.set("titles.custom", tmap);
        y.set("titles.active_slot", data.getActiveTitleSlot());
        y.set("limits.legendary_used", data.getDailyLegendaryUsed());
        y.set("limits.megaboss_used", data.getDailyMegabossUsed());
        try { y.save(f); } catch (IOException ignored) {}
    }
}
