package com.minkang.ultimate.playtimetitles.managers;

import com.minkang.ultimate.playtimetitles.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public class UsageManager {
    private final Main plugin;
    private final RankManager rankManager;
    private File file;
    private FileConfiguration data;
    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    public UsageManager(Main plugin, RankManager rankManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
        this.file = new File(plugin.getDataFolder(), "usage.yml");
        if (!file.exists()) try { file.getParentFile().mkdirs(); file.createNewFile(); } catch (IOException ignored) {}
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    private String todayKey() {
        LocalDate now = LocalDate.now(KST);
        return now.toString();
    }

    public int getUsed(UUID u, String type) {
        String key = u.toString()+"."+todayKey()+"."+type;
        return data.getInt(key, 0);
    }

    public int getLimit(UUID u, String type) {
        RankManager.Rank r = rankManager.getCurrentRank(u);
        if (r == null) return 0;
        if ("legendary".equalsIgnoreCase(type)) return r.legendaryDaily;
        if ("megaboss".equalsIgnoreCase(type)) return r.megabossDaily;
        return 0;
    }

    public boolean canUse(UUID u, String type) {
        int used = getUsed(u, type);
        int limit = getLimit(u, type);
        return used < limit;
    }

    public void addUse(UUID u, String type) {
        String key = u.toString()+"."+todayKey()+"."+type;
        int v = data.getInt(key, 0) + 1;
        data.set(key, v);
        save();
    }

    public void saveAll() { save(); }

    private void save() {
        try { data.save(file); }
        catch (IOException e) { plugin.getLogger().warning("usage.yml 저장 실패: " + e.getMessage()); }
    }
}
