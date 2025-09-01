
package com.minkang.ultimate.commands;

import com.minkang.ultimate.UltimateTitleRank;
import com.minkang.ultimate.util.Texts;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TitleVoucherCommand implements CommandExecutor {

    private final UltimateTitleRank plugin;

    public TitleVoucherCommand(UltimateTitleRank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Texts.color("&c이 명령은 OP만 사용할 수 있습니다."));
            return true;
        }
        int amount = 1;
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("수량")) {
                if (args.length >= 2) {
                    try { amount = Math.max(1, Integer.parseInt(args[1])); } catch (NumberFormatException ignored) {}
                }
            } else {
                try { amount = Math.max(1, Integer.parseInt(args[0])); } catch (NumberFormatException ignored) {}
            }
        }
        ItemStack voucher = plugin.getTitleManager().createCustomTitleVoucher();
        voucher.setAmount(amount);
        if (sender instanceof Player) {
            ((Player) sender).getInventory().addItem(voucher);
        } else {
            sender.sendMessage(Texts.color("&e콘솔에서는 아이템을 지급할 수 없습니다."));
        }
        sender.sendMessage(Texts.color("&a칭호권 지급 x" + amount));
        return true;
    }
}
