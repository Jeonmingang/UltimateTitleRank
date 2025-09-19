package com.minkang.ultimate.playtimetitles.commands;

import com.minkang.ultimate.playtimetitles.Main;
import com.minkang.ultimate.playtimetitles.managers.RankManager;
import com.minkang.ultimate.playtimetitles.managers.RankManager.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommands implements CommandExecutor {
    private final Main plugin;
    private final RankManager rankManager;

    public RankCommands(Main plugin, RankManager rankManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        Player p = (Player)sender;
        String name = command.getName();
        if ("승급".equalsIgnoreCase(name)) {
            rankManager.tryPromote(p);
            p.sendMessage("§a승급 검사 완료!");
            return true;
        }
        if ("승급조건".equalsIgnoreCase(name)) {
            int mins = plugin.getPlaytimeManager().getMinutes(p.getUniqueId());
            Rank cur = rankManager.getCurrentRank(p.getUniqueId());
            Rank next = rankManager.getNextRank(p.getUniqueId());
            p.sendMessage("§7현재 플레이타임: §a" + mins + "분");
            if (cur != null) p.sendMessage("§7현재 등급: " + cur.display);
            if (next != null) {
                int need = next.requiredMinutes - mins;
                if (need < 0) need = 0;
                p.sendMessage("§b다음 등급: " + next.display + " §7(필요: §e" + next.requiredMinutes + "분§7, 남은: §e" + need + "분§7)");
                p.sendMessage("§7다음 등급 접미사: " + next.suffix);
                p.sendMessage("§7전설시간/메가보스 일일 허용: §a" + next.legendaryDaily + "§7 / §a" + next.megabossDaily);
            } else {
                p.sendMessage("§a최고 등급에 도달했습니다!");
            }
            return true;
        }
        return false;
    }
}
