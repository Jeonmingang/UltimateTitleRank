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
    private TitleChatInputListener titleChatInputListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.storage = new YamlStorage(this);
        this.rankService = new RankService(this);
        this.luckPermsHook = new LuckPermsHook();
        this.titleService = new TitleService(this);
        this.titleGUI = new TitleGUI(this, titleService);
        this.limitService = new DailyLimitService(this);
        this.colorChatService = new ColorChatService(this);
        this.colorChatGUI = new ColorChatGUI(this, colorChatService);

        // Commands
        if (getCommand("승급") != null) getCommand("승급").setExecutor(new cmd_Promote(this));
        if (getCommand("칭호") != null) getCommand("칭호").setExecutor(new cmd_TitleRoot(this, titleService, titleGUI));

        // Listeners
        Bukkit.getPluginManager().registerEvents(new TitleGUIListener(this, titleService), this);
        Bukkit.getPluginManager().registerEvents(new CustomTitleItemListener(this, titleService), this);
        Bukkit.getPluginManager().registerEvents(new CommandInterceptListener(this, limitService), this);
        Bukkit.getPluginManager().registerEvents(new TitleTokenListener(this), this);
        this.titleChatInputListener = new TitleChatInputListener(this);
        Bukkit.getPluginManager().registerEvents(this.titleChatInputListener, this);
        Bukkit.getPluginManager().registerEvents(new ColorChatGUIListener(this, colorChatService), this);
        Bukkit.getPluginManager().registerEvents(new ColorTokenListener(this, colorChatService), this);
        Bukkit.getPluginManager().registerEvents(new ColorChatFormatterListener(this, colorChatService), this);
    }

    @Override
    public void onDisable() { }

    // Accessors
    public static Main inst() { return instance; }
    public RankService rankService() { return rankService; }
    public Storage storage() { return storage; }
    public TitleGUI titleGUI() { return titleGUI; }
    public LuckPermsHook luck() { return luckPermsHook; }
    public TitleService titles() { return titleService; }
    public DailyLimitService limitService() { return limitService; }
    public ColorChatService colorChatService() { return colorChatService; }
    public ColorChatGUI colorChatGUI() { return colorChatGUI; }
    public TitleChatInputListener getTitleChatInputListener() { return titleChatInputListener; }

    public String msg(String key) {
        FileConfiguration c = getConfig();
        return c.getString("messages."+key, key);
    }
}
