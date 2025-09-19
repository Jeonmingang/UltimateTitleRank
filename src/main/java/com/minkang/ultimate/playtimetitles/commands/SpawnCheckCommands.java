package com.minkang.ultimate.playtimetitles.commands;

import com.minkang.ultimate.playtimetitles.Main;
import com.minkang.ultimate.playtimetitles.managers.UsageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCheckCommands implements CommandExecutor {
    private final Main plugin;
    private final UsageManager usage;
    private final String mode; // legendary / megaboss / legendary_count / megaboss_count

    public SpawnCheckCommands(Main plugin, UsageManager usage, String mode) {
        this.plugin = plugin;
        this.usage = usage;
        this.mode = mode;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        Player p = (Player)sender;
        if ("legendary_count".equalsIgnoreCase(mode)) {
            int used = usage.getUsed(p.getUniqueId(), "legendary");
            int limit = usage.getLimit(p.getUniqueId(), "legendary");
            p.sendMessage("§7전설시간 사용: §b" + used + "§7 / §a" + limit + " §7(일일)");
            return true;
        }
        if ("megaboss_count".equalsIgnoreCase(mode)) {
            int used = usage.getUsed(p.getUniqueId(), "megaboss");
            int limit = usage.getLimit(p.getUniqueId(), "megaboss");
            p.sendMessage("§7메가보스 사용: §b" + used + "§7 / §a" + limit + " §7(일일)");
            return true;
        }
        if ("legendary".equalsIgnoreCase(mode)) {
            if (!usage.canUse(p.getUniqueId(), "legendary")) {
                p.sendMessage(ChatColor.RED + "일일 사용 가능 횟수를 모두 소모했습니다.");
                return true;
            }
            usage.addUse(p.getUniqueId(), "legendary");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "checkspawns legendary");
            p.sendMessage(ChatColor.GREEN + "전설시간 조회 실행됨.");
            return true;
        }
        if ("megaboss".equalsIgnoreCase(mode)) {
            if (!usage.canUse(p.getUniqueId(), "megaboss")) {
                p.sendMessage(ChatColor.RED + "일일 사용 가능 횟수를 모두 소모했습니다.");
                return true;
            }
            usage.addUse(p.getUniqueId(), "megaboss");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "checkspawns megaboss");
            p.sendMessage(ChatColor.GREEN + "메가보스 조회 실행됨.");
            return true;
        }
        return false;
    }
}
