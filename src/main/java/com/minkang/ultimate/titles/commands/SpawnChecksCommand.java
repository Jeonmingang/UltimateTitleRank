package com.minkang.ultimate.titles.commands;

import com.minkang.ultimate.titles.UltimateTitleRank;
import com.minkang.ultimate.titles.storage.PlayerData;
import com.minkang.ultimate.titles.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpawnChecksCommand implements CommandExecutor {
    private final UltimateTitleRank plugin;
    private final String kind;
    private static final Map<String, Long> cooldowns = new HashMap<String, Long>();

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

        if (kind.equals("legendary") && !p.hasPermission("utr.user.legend")) {
            p.sendMessage(ColorUtil.colorize("&c이 명령을 사용할 권한이 없습니다."));
            return true;
        }
        if (kind.equals("megaboss") && !p.hasPermission("utr.user.megaboss")) {
            p.sendMessage(ColorUtil.colorize("&c이 명령을 사용할 권한이 없습니다."));
            return true;
        }

        final UUID uid = p.getUniqueId();
        final String cdKey = uid.toString() + ":" + kind;
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(cdKey);
        if (last != null && now - last < 1000) {
            p.sendMessage(ColorUtil.colorize("&c너무 빠르게 입력했습니다. 잠시 후 다시 시도하세요."));
            return true;
        }

        int leftAfterConsume = -1;
        synchronized (plugin.getStorage()) {
            PlayerData pd = plugin.getStorage().get(uid);
            int allow = getDailyLimit(pd.getCurrentRankId());
            int used = (kind.equals("legendary") ? pd.getLegendaryUsedToday() : pd.getMegabossUsedToday());
            if (used >= allow) {
                p.sendMessage(ColorUtil.colorize("&c오늘 사용 가능한 횟수를 모두 사용했습니다. (허용: " + allow + "회)"));
                return true;
            }
            if (kind.equals("legendary")) pd.incLegendary(); else pd.incMegaboss();
            plugin.getStorage().save();
            int usedNow = (kind.equals("legendary") ? pd.getLegendaryUsedToday() : pd.getMegabossUsedToday());
            leftAfterConsume = max(0, allow - usedNow);
        }

        cooldowns.put(cdKey, now);

        String cmdToRun = (kind.equals("legendary") ? "checkspawns legendary" : "checkspawns megaboss");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdToRun);
        p.sendMessage(ColorUtil.colorize("&a명령 실행됨: &f/" + cmdToRun + " &7| 남은 횟수: &e" + leftAfterConsume + "회"));
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

    private int max(int a, int b) { return a > b ? a : b; }
}
