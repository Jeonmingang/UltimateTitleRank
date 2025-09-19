
package com.minkang.ultimate.titles;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private RankService rankService;
    private Storage storage;
    private TitleGUI titleGUI;
    private LuckPermsHook luckPermsHook;
    private TitleService titleService;
    private DailyLimitService limitService;
    private ColorChatService colorChatService;
    private ColorChatGUI colorChatGUI;

    public static Main get() { return instance; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("messages.yml", false);

        this.storage = new YamlStorage(this);
        this.rankService = new RankService(this);
        this.luckPermsHook = new LuckPermsHook();
        this.titleService = new TitleService(this);
        this.titleGUI = new TitleGUI(this, titleService);
        this.limitService = new DailyLimitService(this);
        this.colorChatService = new ColorChatService(this);
        this.colorChatGUI = new ColorChatGUI(this, colorChatService);

        getCommand("승급").setExecutor(new cmd_Promote(this));
        getCommand("칭호").setExecutor(new cmd_Title(this, titleGUI));
        getCommand("칭호생성").setExecutor(new cmd_AdminCreate(this));
        getCommand("칭호삭제").setExecutor(new cmd_AdminDelete(this));
        getCommand("칭호목록").setExecutor(new cmd_AdminList(this));
        getCommand("칭호아이템").setExecutor(new cmd_AdminItem(this));
        getCommand("색깔채팅").setExecutor(new ColorChatCommand(this, colorChatService, colorChatGUI));

        Bukkit.getPluginManager().registerEvents(new TitleGUIListener(this, titleService), this);
        Bukkit.getPluginManager().registerEvents(new CustomTitleItemListener(this, titleService), this);
        Bukkit.getPluginManager().registerEvents(new CommandInterceptListener(this, limitService), this);
        Bukkit.getPluginManager().registerEvents(new ColorChatGUIListener(this, colorChatService), this);
        Bukkit.getPluginManager().registerEvents(new ColorTokenListener(this, colorChatService), this);
        Bukkit.getPluginManager().registerEvents(new ColorChatFormatterListener(this, colorChatService), this);

        // periodic rank check
        Bukkit.getScheduler().runTaskTimer(this, () ->
            Bukkit.getOnlinePlayers().forEach(p -> rankService.checkAndApplyRank(p))
        , 20L * 10, 20L * 60);

        getLogger().info("UltimateTitleRank enabled.");
    }

    @Override
    public void onDisable() {
        if (storage != null) storage.flush();
    }

    public Storage storage() { return storage; }
    public RankService rankService() { return rankService; }
    public LuckPermsHook luck() { return luckPermsHook; }
    public TitleService titleService() { return titleService; }
    public DailyLimitService limitService() { return limitService; }

    public String msg(String path) {
        FileConfiguration m = YamlFiles.getMessages(this);
        return Chat.color(m.getString(path, path));
    }
}
