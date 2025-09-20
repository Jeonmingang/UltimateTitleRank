
package com.minkang.ultimate.titles.commands;

import com.minkang.ultimate.titles.UltimateTitleRank;
import com.minkang.ultimate.titles.storage.PlayerData;
import com.minkang.ultimate.titles.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TitleCommand implements CommandExecutor, TabCompleter {
    private final UltimateTitleRank plugin;

    public TitleCommand(UltimateTitleRank plugin) {
        this.plugin = plugin;
    }

    public static String decodeHiddenKey(ItemStack it) {
        if (it == null) return "";
        ItemMeta im = it.getItemMeta();
        if (im == null) return "";
        List<String> lore = im.getLore();
        if (lore == null) return "";
        for (String l : lore) {
            if (ChatColor.stripColor(l).startsWith("KEY:")) {
                return ChatColor.stripColor(l).substring(4);
            }
        }
        return "";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.colorize("&e사용법: /칭호 열기 | /칭호 생성 <이름> <번호> | /칭호 삭제 <플레이어> <번호> | /칭호 삭제 <번호> | /칭호 목록 | /칭호 아이템"));
            return true;
        }
        String sub = args[0];
        if (sub.equalsIgnoreCase("열기")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("플레이어만 사용 가능합니다.");
                return true;
            }
            openTitleGui((Player)sender);
            return true;
        }
        if (sub.equalsIgnoreCase("아이템")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("플레이어만 사용 가능합니다.");
                return true;
            }
            Player p = (Player)sender;
            ItemStack inHand = p.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType() == Material.AIR) {
                p.sendMessage(ColorUtil.colorize("&c손에 아이템을 들고 사용하세요."));
                return true;
            }
            ItemMeta im = inHand.getItemMeta();
            if (im == null) im = Bukkit.getItemFactory().getItemMeta(inHand.getType());
            im.setDisplayName(ColorUtil.colorize(plugin.getConfig().getString("custom_title_voucher.name", "&d칭호 커스텀권")));
            java.util.List<String> lore = plugin.getConfig().getStringList("custom_title_voucher.lore");
            java.util.List<String> clore = new java.util.ArrayList<String>();
            for (String s : lore) clore.add(ColorUtil.colorize(s));
            im.setLore(clore);
            inHand.setItemMeta(im);
            p.sendMessage(ColorUtil.colorize("&a해당 아이템이 '칭호 커스텀권'으로 설정되었습니다."));
            return true;
        }
        if (sub.equalsIgnoreCase("생성")) {
            if (args.length < 3) {
                sender.sendMessage(ColorUtil.colorize("&c사용법: /칭호 생성 <이름> <번호>"));
                return true;
            }
            String name = ColorUtil.colorize(args[1]);
            int slot;
            try { slot = Integer.parseInt(args[2]); } catch (Exception ex) {
                sender.sendMessage(ColorUtil.colorize("&c번호는 정수여야 합니다."));
                return true;
            }
            java.util.List<String> list = plugin.getConfig().getStringList("global_titles");
            if (list == null) list = new java.util.ArrayList<String>();
            while (list.size() <= slot) list.add("");
            list.set(slot, name);
            plugin.getConfig().set("global_titles", list);
            plugin.saveConfig();
            sender.sendMessage(ColorUtil.colorize("&a칭호가 생성되었습니다. 슬롯: " + slot + " 이름: " + name));
            return true;
        }
        if (sub.equalsIgnoreCase("삭제")) {
            if (args.length == 2) {
                int slot;
                try { slot = Integer.parseInt(args[1]); } catch (Exception ex) {
                    sender.sendMessage(ColorUtil.colorize("&c사용법: /칭호 삭제 <번호>"));
                    return true;
                }
                java.util.List<String> list = plugin.getConfig().getStringList("global_titles");
                if (list != null && slot < list.size()) {
                    list.set(slot, "");
                    plugin.getConfig().set("global_titles", list);
                    plugin.saveConfig();
                    sender.sendMessage(ColorUtil.colorize("&a전역 칭호 슬롯 " + slot + " 이(가) 삭제되었습니다."));
                } else {
                    sender.sendMessage(ColorUtil.colorize("&c해당 슬롯이 존재하지 않습니다."));
                }
                return true;
            }
            if (args.length >= 3) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                int slot;
                try { slot = Integer.parseInt(args[2]); } catch (Exception ex) {
                    sender.sendMessage(ColorUtil.colorize("&c번호는 정수여야 합니다."));
                    return true;
                }
                com.minkang.ultimate.titles.storage.PlayerData pd = plugin.getStorage().get(target.getUniqueId());
                java.util.List<String> owned = pd.getOwnedTitles();
                if (slot >= 0 && slot < owned.size()) {
                    owned.remove(slot);
                    sender.sendMessage(ColorUtil.colorize("&a" + target.getName() + " 의 칭호 슬롯 " + slot + " 삭제됨."));
                } else {
                    sender.sendMessage(ColorUtil.colorize("&c해당 슬롯이 없습니다."));
                }
                return true;
            }
            sender.sendMessage(ColorUtil.colorize("&c사용법: /칭호 삭제 <플레이어> <번호> 또는 /칭호 삭제 <번호>"));
            return true;
        }
        if (sub.equalsIgnoreCase("목록")) {
            java.util.List<String> list = plugin.getConfig().getStringList("global_titles");
            sender.sendMessage(ColorUtil.colorize("&e전역 칭호 목록:"));
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    String t = list.get(i);
                    if (t != null && !t.isEmpty()) {
                        sender.sendMessage(ColorUtil.colorize("&7슬롯 " + i + ": " + t));
                    }
                }
            }
            return true;
        }
        sender.sendMessage(ColorUtil.colorize("&c알 수 없는 하위 명령입니다."));
        return true;
    }

    public void openTitleGui(Player p) {
        String title = ColorUtil.colorize(plugin.getConfig().getString("title_gui.title", "&8칭호 선택"));
        int size = plugin.getConfig().getInt("title_gui.size", 54);
        Inventory inv = Bukkit.createInventory(null, size, title);

        PlayerData pd = plugin.getStorage().get(p.getUniqueId());
        java.util.List<String> owned = pd.getOwnedTitles();
        int slot = 0;
        for (String t : owned) {
            if (t == null || t.isEmpty()) continue;
            ItemStack it = new ItemStack(Material.NAME_TAG);
            ItemMeta im = it.getItemMeta();
            im.setDisplayName(ColorUtil.colorize(t));
            java.util.List<String> lore = new java.util.ArrayList<String>();
            lore.add(ColorUtil.colorize("&7클릭하여 적용"));
            im.setLore(lore);
            it.setItemMeta(im);
            if (slot < size) inv.setItem(slot++, it);
        }
        java.util.List<String> global = plugin.getConfig().getStringList("global_titles");
        if (global != null) {
            for (String t : global) {
                if (t == null || t.isEmpty()) continue;
                ItemStack it = new ItemStack(Material.NAME_TAG);
                ItemMeta im = it.getItemMeta();
                im.setDisplayName(ColorUtil.colorize(t));
                java.util.List<String> lore = new java.util.ArrayList<String>();
                lore.add(ColorUtil.colorize("&7클릭하여 적용"));
                im.setLore(lore);
                it.setItemMeta(im);
                if (slot < size) inv.setItem(slot++, it);
            }
        }
        p.openInventory(inv);
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        java.util.List<String> out = new java.util.ArrayList<String>();
        if (args.length == 1) {
            out.add("열기"); out.add("생성"); out.add("삭제"); out.add("목록"); out.add("아이템");
        }
        return out;
    }
}
