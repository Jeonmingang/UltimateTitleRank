
package com.minkang.ultimate.titles;

import com.minkang.ultimate.titles.commands.ColorChatCommand;
import com.minkang.ultimate.titles.commands.RankCommand;
import com.minkang.ultimate.titles.commands.SpawnChecksCommand;
import com.minkang.ultimate.titles.commands.TitleCommand;
import com.minkang.ultimate.titles.listeners.ChatListener;
import com.minkang.ultimate.titles.listeners.GuiListener;
import com.minkang.ultimate.titles.listeners.PlaytimeListener;
import com.minkang.ultimate.titles.listeners.VoucherListener;
import com.minkang.ultimate.titles.storage.Storage;
import com.minkang.ultimate.titles.util.LPUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;

public class UltimateTitleRank extends JavaPlugin {

    private static UltimateTitleRank instance;
    private Storage storage;
    private LPUtils lpUtils;

    public static UltimateTitleRank getInstance() {
        return instance;
    }

    public Storage getStorage() {
        return storage;
    }

    public LPUtils getLpUtils() {
        return lpUtils;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.storage = new Storage(this);
        this.lpUtils = new LPUtils(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new PlaytimeListener(this), this);
        getServer().getPluginManager().registerEvents(new VoucherListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // Commands
        getCommand("승급").setExecutor(new RankCommand(this));
        getCommand("전설시간횟수").setExecutor(new SpawnChecksCommand(this, "legendary"));
        getCommand("메가보스횟수").setExecutor(new SpawnChecksCommand(this, "megaboss"));
        TitleCommand titleCmd = new TitleCommand(this);
        getCommand("칭호").setExecutor(titleCmd);
        getCommand("칭호").setTabCompleter(titleCmd);
        ColorChatCommand colorCmd = new ColorChatCommand(this);
        getCommand("색깔채팅").setExecutor(colorCmd);
        getCommand("색깔채팅").setTabCompleter(colorCmd);

        // Playtime tick task each minute
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                PlaytimeListener.tickOnlinePlayers();
            }
        }, 20L * 60L, 20L * 60L);

        // Daily reset task check every 5 minutes
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                dailyResetIfNeeded();
            }
        }, 20L * 60L * 5L, 20L * 60L * 5L);

        getLogger().info("UltimateTitleRank v6 enabled.");
    }

    @Override
    public void onDisable() {
        storage.save();
    }

    public void dailyResetIfNeeded() {
        FileConfiguration cfg = getConfig();
        String tz = cfg.getString("general.timezone", "Asia/Seoul");
        int resetHour = cfg.getInt("general.daily_reset_hour", 4);
        TimeZone timeZone = TimeZone.getTimeZone(tz);
        Calendar cal = Calendar.getInstance(timeZone);
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        int d = cal.get(Calendar.DAY_OF_MONTH);
        String today = String.format("%04d-%02d-%02d", y, m, d);

        if (!storage.getLastResetDate().equals(today)) {
            // only reset after resetHour
            if (cal.get(Calendar.HOUR_OF_DAY) >= resetHour) {
                storage.resetDailyCounters(today);
                getLogger().info("Daily counters reset at " + today + " " + resetHour + ":00 " + tz);
            }
        }
    }
}
