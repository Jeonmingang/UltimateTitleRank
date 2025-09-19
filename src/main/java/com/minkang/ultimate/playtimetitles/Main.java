package com.minkang.ultimate.playtimetitles;

import com.minkang.ultimate.playtimetitles.managers.*;
import com.minkang.ultimate.playtimetitles.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private PlaytimeManager playtimeManager;
    private RankManager rankManager;
    private UsageManager usageManager;
    private TitleManager titleManager;
    private ColorChatManager colorChatManager;
    private LuckPermsBridge lp;

    public static Main getInstance() { return instance; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        lp = new LuckPermsBridge(this);
        playtimeManager = new PlaytimeManager(this);
        rankManager = new RankManager(this, lp);
        usageManager = new UsageManager(this, rankManager);
        colorChatManager = new ColorChatManager(this);
        titleManager = new TitleManager(this, lp, colorChatManager);

        // Command executors
        getCommand("승급").setExecutor(new RankCommands(this, rankManager));
        getCommand("승급조건").setExecutor(new RankCommands(this, rankManager));
        getCommand("전설시간").setExecutor(new SpawnCheckCommands(this, usageManager, "legendary"));
        getCommand("전설시간횟수").setExecutor(new SpawnCheckCommands(this, usageManager, "legendary_count"));
        getCommand("메가보스").setExecutor(new SpawnCheckCommands(this, usageManager, "megaboss"));
        getCommand("메가보스횟수").setExecutor(new SpawnCheckCommands(this, usageManager, "megaboss_count"));
        getCommand("칭호").setExecutor(new TitleCommands(this, titleManager));
        getCommand("색깔채팅").setExecutor(new ColorChatCommands(this, colorChatManager));

        getServer().getPluginManager().registerEvents(titleManager, this);
        playtimeManager.start(); // start scheduler

        getLogger().info("[UltimatePlaytimeTitles] Enabled.");
        if (!lp.isAvailable()) {
            getLogger().warning("LuckPerms가 감지되지 않았습니다. 그룹/접미사 기능이 제한됩니다.");
        }
    }

    @Override
    public void onDisable() {
        if (playtimeManager != null) playtimeManager.saveAll();
        if (titleManager != null) titleManager.saveAll();
        if (colorChatManager != null) colorChatManager.saveAll();
        if (usageManager != null) usageManager.saveAll();
        getLogger().info("[UltimatePlaytimeTitles] Disabled.");
    }

    public PlaytimeManager getPlaytimeManager() { return playtimeManager; }
    public RankManager getRankManager() { return rankManager; }
    public UsageManager getUsageManager() { return usageManager; }
    public TitleManager getTitleManager() { return titleManager; }
    public ColorChatManager getColorChatManager() { return colorChatManager; }
    public LuckPermsBridge getLuckPerms() { return lp; }
}
