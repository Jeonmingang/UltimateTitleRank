package com.minkang.ultimate.playtimetitles.commands;

import com.minkang.ultimate.playtimetitles.Main;
import com.minkang.ultimate.playtimetitles.managers.ColorChatManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ColorChatCommands implements CommandExecutor {
    private final Main plugin;
    private final ColorChatManager colors;

    public ColorChatCommands(Main plugin, ColorChatManager colors) {
        this.plugin = plugin;
        this.colors = colors;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7/색깔채팅 열기 | /색깔채팅 지급 <색깔> [플레이어] | /색깔채팅 제거 <플레이어> <색깔> | /색깔채팅 목록 [플레이어]");
            return true;
        }
        String sub = args[0];
        if ("열기".equalsIgnoreCase(sub)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("플레이어만 사용 가능합니다.");
                return true;
            }
            Player p = (Player)sender;
            colors.openGui(p);
            return true;
        }
        if ("지급".equalsIgnoreCase(sub)) {
            if (!sender.isOp()) { sender.sendMessage(ChatColor.RED + "오피만 사용 가능합니다."); return true; }
            if (args.length < 2) { sender.sendMessage("/색깔채팅 지급 <색깔> [플레이어]"); return true; }
            String colorKr = args[1];
            Player target = (args.length >= 3) ? Bukkit.getPlayer(args[2]) : (sender instanceof Player ? (Player)sender : null);
            if (target == null) { sender.sendMessage("타깃 플레이어를 찾을 수 없습니다."); return true; }
            colors.giveColor(target, colorKr);
            sender.sendMessage("§a지급 완료: " + target.getName() + " -> " + colorKr);
            return true;
        }
        if ("제거".equalsIgnoreCase(sub)) {
            if (!sender.isOp()) { sender.sendMessage(ChatColor.RED + "오피만 사용 가능합니다."); return true; }
            if (args.length < 3) { sender.sendMessage("/색깔채팅 제거 <플레이어> <색깔>"); return true; }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) { sender.sendMessage("플레이어 오프라인"); return true; }
            String colorKr = args[2];
            colors.removeColor(target, colorKr);
            sender.sendMessage("§c제거 완료: " + target.getName() + " -> " + colorKr);
            return true;
        }
        if ("목록".equalsIgnoreCase(sub)) {
            Player target;
            if (args.length >= 2) {
                target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage("플레이어 오프라인"); return true; }
            } else {
                if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용"); return true; }
                target = (Player)sender;
            }
            sender.sendMessage("§7허용 색깔: " + String.join(", ", colors.getColors(target)));
            return true;
        }
        sender.sendMessage("§c알 수 없는 하위 명령어.");
        return true;
    }
}
