package com.minkang.ultimate.titles.commands;

import com.minkang.ultimate.titles.UltimateTitleRank;
import com.minkang.ultimate.titles.storage.PlayerData;
import com.minkang.ultimate.titles.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {
    private final UltimateTitleRank plugin;
    public RankCommand(UltimateTitleRank plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        Player p = (Player)sender;
        PlayerData pd = plugin.getStorage().get(p.getUniqueId());
        int minutes = pd.getMinutesPlayed();

        java.util.List<java.util.Map<?, ?>> ranks = plugin.getConfig().getMapList("ranks");
        java.util.Collections.sort(ranks, new java.util.Comparator<java.util.Map<?, ?>>() {
            @Override
            public int compare(java.util.Map<?, ?> a, java.util.Map<?, ?> b) {
                int ra = ((Number)a.get("required_minutes")).intValue();
                int rb = ((Number)b.get("required_minutes")).intValue();
                return Integer.compare(ra, rb);
            }
        });

        String nextName = "최고등급";
        int need = -1;
        boolean past = true;
        for (java.util.Map<?, ?> m : ranks) {
            int req = ((Number)m.get("required_minutes")).intValue();
            String id = String.valueOf(m.get("id"));
            String disp = String.valueOf(m.get("display"));
            if (minutes < req) {
                nextName = disp + " (&7" + id + "&r)";
                need = req - minutes;
                past = false;
                break;
            }
        }
        String msg;
        if (past) {
            msg = "&6현재 최고 등급입니다! 플레이타임: &e" + minutes + "분";
        } else {
            msg = "&e다음 등급까지 &c" + need + "분 &e남음: " + nextName;
        }
        p.sendMessage(ColorUtil.colorize(msg));
        return true;
    }
}
