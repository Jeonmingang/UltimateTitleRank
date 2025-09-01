
package com.minkang.ultimate;

import com.minkang.ultimate.commands.RankCommand;
import com.minkang.ultimate.commands.TitleCommand;
import com.minkang.ultimate.listeners.BookListener;
import com.minkang.ultimate.listeners.ChatListener;
import com.minkang.ultimate.managers.RankManager;
import com.minkang.ultimate.managers.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class UltimateTitleRank extends JavaPlugin {

    private static UltimateTitleRank instance;
    private TitleManager titleManager;
    private RankManager rankManager;

    public static UltimateTitleRank getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getDataFolder().mkdirs();

        this.titleManager = new TitleManager(this);
        this.titleManager.load();

        this.rankManager = new RankManager(this);
        this.rankManager.load();

        // Commands
        getCommand("title").setExecutor(new TitleCommand(this));
        getCommand("rankinfo").setExecutor(new RankCommand(this));

        // Listeners
        if (getConfig().getBoolean("chat.format_enabled", true)) {
            Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        }
        Bukkit.getPluginManager().registerEvents(new BookListener(this), this);

        // Schedulers
        this.rankManager.startScheduler();
        this.titleManager.startAutoSave();

        getLogger().info("UltimateTitleRank enabled.");
    }

    @Override
    public void onDisable() {
        if (titleManager != null) titleManager.save();
        if (rankManager != null) rankManager.shutdown();
        getLogger().info("UltimateTitleRank disabled.");
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }
}
