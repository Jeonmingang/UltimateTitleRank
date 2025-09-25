package com.minkang.ultimate.autorank;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class UltimateAutoRankPlugin extends JavaPlugin implements org.bukkit.command.TabExecutor {

    private PromotionManager promotionManager;
    private int taskId = -1;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Dependency checks
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            String msg = getConfig().getString("messages.missing_lp", "&cLuckPerms 가 필요합니다.");
            getLogger().severe(color(msg));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null
                || Bukkit.getPluginManager().getPlugin("Playtime") == null) {
            String msg = getConfig().getString("messages.missing_papi", "&cPlaceholderAPI 또는 Playtime 플러그인이 필요합니다.");
            getLogger().severe(color(msg));
            // Keep enabled so /rank status can still tell user? Disable to be clear.
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.promotionManager = new PromotionManager(this);

        int seconds = Math.max(10, getConfig().getInt("check-interval-seconds", 60));
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                promotionManager.tickOnlinePlayers();
            }
        }, 20L * 10, 20L * seconds); // first run after 10s

        if (getCommand("rank") != null) { getCommand("rank").setExecutor(this); getCommand("rank").setTabCompleter(this); }
        getLogger().info("UltimateAutoRank enabled. Interval: " + seconds + "s");
    }

    @Override
    public void onDisable() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName().toLowerCase();
        boolean ok = "rank".equals(name);
        if (!ok) {
            ok = "승급".equals(name);
        }
        if (!ok) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }
        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage(color(prefix() + "&7사용법: &f/" + label + " 조건"));
            return true;
        }

        String sub = args[0];
        boolean isCond = "조건".equals(sub);
        if (!isCond) {
            isCond = "status".equalsIgnoreCase(sub);
        }
        if (!isCond) {
            p.sendMessage(color(prefix() + "&7사용법: &f/" + label + " 조건"));
            return true;
        }

        if (promotionManager == null || !promotionManager.isReady()) {
            p.sendMessage(color(getConfig().getString("messages.missing_papi")));
            return true;
        }

        long seconds = promotionManager.resolvePlaytimeSeconds(p);
        if (seconds < 0) {
            p.sendMessage(color(prefix() + "&c플레이타임 조회에 실패했습니다. PlaceholderAPI/Playtime 설치 및 확장을 확인하세요."));
            return true;
        }

        java.util.List<String> groups = new java.util.ArrayList<>();
        // Show next rule vs current groups
        com.minkang.ultimate.autorank.PromotionManager.RankRule next =
                promotionManager.getNextRuleFor(seconds, groups);
        if (next == null) {
            p.sendMessage(color(getConfig().getString("messages.maxed")));
            return true;
        }

        long remain = next.requiredSeconds - seconds;
        if (remain < 0) remain = 0;
        String remainStr = com.minkang.ultimate.autorank.TimeUtil.formatSeconds(remain);
        String msg = getConfig().getString("messages.status",
                "&e다음 등급: &b%next_group% &7| &e남은 플레이타임: &b%remaining%");
        msg = msg.replace("%next_group%", next.promoteTo)
                 .replace("%remaining%", remainStr);
        p.sendMessage(color(prefix() + msg));
        return true;
    }

    private String prefix() {
        return getConfig().getString("messages.prefix", "&7[&a승급&7]&r ");
    }

    private static String color(String s) {
        return s == null ? "" : org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }
}

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        java.util.List<String> list = new java.util.ArrayList<String>();
        String name = command.getName().toLowerCase();
        boolean ok = "rank".equals(name);
        if (!ok) {
            ok = "승급".equals(name);
        }
        if (!ok) {
            return list;
        }
        if (args.length == 1) {
            if ("조건".startsWith(args[0])) list.add("조건");
            if ("status".startsWith(args[0].toLowerCase())) list.add("status");
        }
        return list;
    }
