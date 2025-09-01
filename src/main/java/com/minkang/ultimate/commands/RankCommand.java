
package com.minkang.ultimate.commands;

import com.minkang.ultimate.UltimateTitleRank;
import com.minkang.ultimate.managers.RankManager;
import com.minkang.ultimate.util.Texts;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {

    private final UltimateTitleRank plugin;
    private final RankManager ranks;

    public RankCommand(UltimateTitleRank plugin) {
        this.plugin = plugin;
        this.ranks = plugin.getRankManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }
        Player p = (Player) sender;
        String msg = ranks.formatRankInfo(p);
        p.sendMessage(msg);
        return true;
    }
}
