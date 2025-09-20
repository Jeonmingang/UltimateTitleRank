package com.minkang.ultimate.titles.commands;

import com.minkang.ultimate.titles.UltimateTitleRank;
import com.minkang.ultimate.titles.storage.PlayerData;
import com.minkang.ultimate.titles.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnChecksCommand implements CommandExecutor {
    private final UltimateTitleRank plugin;
    private final String kind;

    public SpawnChecksCommand(UltimateTitleRank plugin, String kind) {
        this.plugin = plugin;
        this.kind = kind;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        Player p = (Player)sender;
        PlayerData pd = plugin.getStorage().get(p.getUniqueId());

        int allow = getDailyLimit(pd.getCurrentRankId());
        int used = kind.equals("legendary") ? pd.getLegendaryUsedToday() : pd.getMegabossUsedToday();
        int left = allow - used;
        if (left <= 0) {
            p.sendMessage(ColorUtil.colorize("&c오늘 사용 가능한 횟수를 모두 사용했습니다. (허용: " + allow + "회)"));
            return true;
        }

        String cmd = kind.equals("legendary") ? "checkspawns legendary" : "checkspawns megaboss";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        if (kind.equals("legendary")) pd.incLegendary(); else pd.incMegaboss();
        left = left - 1;
        p.sendMessage(ColorUtil.colorize("&a명령 실행됨: &f/" + cmd + " &7| 남은 횟수: &e" + left + "회"));
        plugin.getStorage().save();
        return true;
    }

    private int getDailyLimit(String rankId) {
        java.util.List<java.util.Map<?, ?>> ranks = plugin.getConfig().getMapList("ranks");
        for (java.util.Map<?, ?> m : ranks) {
            String id = String.valueOf(m.get("id"));
            if (id.equals(rankId)) {
                java.util.Map limits = (java.util.Map)((java.util.Map)m.get("daily_limits"));
                if (kind.equals("legendary")) {
                    return ((Number)limits.get("legendary_checks")).intValue();
                } else {
                    return ((Number)limits.get("megaboss_checks")).intValue();
                }
            }
        }
        if (!ranks.isEmpty()) {
            java.util.Map<?, ?> m = ranks.get(0);
            java.util.Map limits = (java.util.Map)((java.util.Map)m.get("daily_limits"));
            if (kind.equals("legendary")) {
                return ((Number)limits.get("legendary_checks")).intValue();
            } else {
                return ((Number)limits.get("megaboss_checks")).intValue();
            }
        }
        return 0;
    }
}
