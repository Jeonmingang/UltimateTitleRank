package com.minkang.ultimate.playtimetitles.commands;

import com.minkang.ultimate.playtimetitles.Main;
import com.minkang.ultimate.playtimetitles.managers.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TitleCommands implements CommandExecutor {
    private final Main plugin;
    private final TitleManager titles;

    public TitleCommands(Main plugin, TitleManager titles) {
        this.plugin = plugin;
        this.titles = titles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7/칭호 열기 | /칭호 생성 <이름> <번호> | /칭호 삭제 <플레이어> <번호> | /칭호 삭제 <번호> | /칭호 목록 | /칭호 아이템");
            return true;
        }
        String sub = args[0];
        if ("열기".equalsIgnoreCase(sub)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("플레이어만 사용 가능합니다.");
                return true;
            }
            Player p = (Player)sender;
            titles.openGui(p);
            return true;
        }
        if ("아이템".equalsIgnoreCase(sub)) {
            if (!sender.isOp()) { sender.sendMessage(ChatColor.RED + "오피만 사용 가능합니다."); return true; }
            if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용"); return true; }
            Player p = (Player)sender;
            ItemStack hand = p.getInventory().getItemInMainHand();
            titles.setCustomTicketFromItem(p, hand);
            return true;
        }
        if ("생성".equalsIgnoreCase(sub)) {
            if (!sender.isOp()) { sender.sendMessage(ChatColor.RED + "오피만 사용 가능합니다."); return true; }
            if (args.length < 3) { sender.sendMessage("/칭호 생성 <이름> <번호>"); return true; }
            String name = ChatColor.translateAlternateColorCodes('&', args[1]);
            int num;
            try { num = Integer.parseInt(args[2]); }
            catch (Exception ex) { sender.sendMessage("번호는 숫자여야 합니다."); return true; }
            titles.createGlobalTitle(name, num);
            sender.sendMessage("§a전역 칭호 생성 #" + num + ": " + name);
            return true;
        }
        if ("삭제".equalsIgnoreCase(sub)) {
            if (!sender.isOp()) { sender.sendMessage(ChatColor.RED + "오피만 사용 가능합니다."); return true; }
            if (args.length == 2) {
                // /칭호 삭제 <번호>
                int num;
                try { num = Integer.parseInt(args[1]); }
                catch (Exception ex) { sender.sendMessage("번호는 숫자여야 합니다."); return true; }
                titles.deleteGlobalTitle(num);
                sender.sendMessage("§c전역 칭호 삭제 #" + num);
                return true;
            }
            if (args.length == 3) {
                // /칭호 삭제 <플레이어> <번호> (보유 제거)
                Player t = Bukkit.getPlayer(args[1]);
                if (t == null) { sender.sendMessage("플레이어 오프라인"); return true; }
                sender.sendMessage("§7현재 버전은 전역 슬롯 삭제만 지원합니다. 보유 제거는 차기 버전에 추가됩니다.");
                return true;
            }
            sender.sendMessage("/칭호 삭제 <번호> | /칭호 삭제 <플레이어> <번호>");
            return true;
        }
        if ("목록".equalsIgnoreCase(sub)) {
            for (String line : titles.listGlobalTitles()) {
                sender.sendMessage("§7" + line);
            }
            return true;
        }
        sender.sendMessage("§c알 수 없는 하위 명령어.");
        return true;
    }
}
