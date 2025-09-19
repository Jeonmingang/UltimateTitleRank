package com.minkang.ultimate.playtimetitles.managers;

import com.minkang.ultimate.playtimetitles.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlaytimeManager {
    private final Main plugin;
    private final Map<UUID, Integer> minutes = new HashMap<>();
    private File file;
    private FileConfiguration data;

    public PlaytimeManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        if (!file.exists()) {
            try { file.getParentFile().mkdirs(); file.createNewFile(); }
            catch (IOException ignored) {}
        }
        this.data = YamlConfiguration.loadConfiguration(file);
        // preload online
        for (Player p : Bukkit.getOnlinePlayers()) {
            minutes.put(p.getUniqueId(), data.getInt(p.getUniqueId().toString()+".minutes", 0));
        }
    }

    public void start() {
        int interval = plugin.getConfig().getInt("playtime.track_interval_seconds", 60);
        new BukkitRunnable(){
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    UUID u = p.getUniqueId();
                    int cur = minutes.containsKey(u) ? minutes.get(u) : data.getInt(u.toString()+".minutes", 0);
                    cur += interval / 60; // only whole minutes
                    minutes.put(u, cur);
                    data.set(u.toString()+".minutes", cur);
                }
                save();
            }
        }.runTaskTimer(plugin, 20L*interval, 20L*interval);
    }

    public int getMinutes(UUID u) {
        if (minutes.containsKey(u)) return minutes.get(u);
        return data.getInt(u.toString()+".minutes", 0);
    }

    public void setMinutes(UUID u, int mins) {
        minutes.put(u, mins);
        data.set(u.toString()+".minutes", mins);
    }

    public void saveAll() { save(); }

    private void save() {
        try { data.save(file); }
        catch (IOException e) { plugin.getLogger().warning("players.yml 저장 실패: " + e.getMessage()); }
    }

    public FileConfiguration getData() { return data; }
    public File getFile() { return file; }
}
