
package com.minkang.ultimate.commands;

import com.minkang.ultimate.UltimateTitleRank;
import com.minkang.ultimate.managers.TitleManager;
import com.minkang.ultimate.util.Texts;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TitleCommand implements CommandExecutor {

    private final UltimateTitleRank plugin;
    private final TitleManager titles;

    public TitleCommand(UltimateTitleRank plugin) {
        this.plugin = plugin;
        this.titles = plugin.getTitleManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("플레이어만 사용할 수 있습니다.");
                return true;
            }
            Player p = (Player) sender;
            titles.openTitleGUI(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("생성")) {
            if (!sender.isOp()) {
                sender.sendMessage(Texts.color("&c이 명령은 OP만 사용할 수 있습니다."));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(Texts.color("&e사용법: /칭호 생성 <칭호이름(색코드& 허용)>"));
                return true;
            }
            String t = String.join(" ", java.util.Arrays.copyOfRange(args,1,args.length));
            // give title book to OP
            ItemStack book = titles.createTitleBook(t);
            if (sender instanceof Player) {
                ((Player) sender).getInventory().addItem(book);
            } else {
                sender.sendMessage(Texts.color("&e콘솔에서는 아이템을 지급할 수 없습니다."));
            }
            // add to global titles registry only (not to a specific player)
            plugin.getTitleManager().addGlobalTitle(t);
            sender.sendMessage(Texts.color("&a칭호북 생성: &f" + t));
            return true;
        }

        
        if (args[0].equalsIgnoreCase("해제")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("플레이어만 사용할 수 있습니다.");
                return true;
            }
            Player p = (Player) sender;
            plugin.getTitleManager().setSelectedTitle(p.getUniqueId(), null);
            p.sendMessage(Texts.color("&7칭호를 해제했습니다."));
            return true;
        }

        if (args[0].equalsIgnoreCase("목록")) {
            if (!sender.isOp()) {
                sender.sendMessage(Texts.color("&c이 명령은 OP만 사용할 수 있습니다."));
                return true;
            }
            List<String> all = plugin.getTitleManager().getAllTitles();
            sender.sendMessage(Texts.color("&e지금까지 생성된 칭호 (" + all.size() + "개):"));
            if (all.isEmpty()) {
                sender.sendMessage(Texts.color("&7(비어있음)"));
            } else {
                for (String t : all) {
                    sender.sendMessage(Texts.color("&7- &f" + t));
                }
            }
            return true;
        }

        // default
        sender.sendMessage(Texts.color("&e사용법: /칭호, /칭호 생성 <이름>, /칭호 목록, /칭호 해제"));
        return true;
    }
}
