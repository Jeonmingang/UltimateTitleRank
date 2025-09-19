package com.minkang.ultimate.titles;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

class cmd_Promote implements CommandExecutor {
    private final Main plugin;
    public cmd_Promote(Main p){ this.plugin=p; }
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player)) { s.sendMessage(plugin.msg("not_player")); return true; }
        Player p=(Player)s;
        int minutes = plugin.rankService().getPlayMinutes(p);
        Rank now = plugin.rankService().getRankByMinutes(minutes);
        Rank next = plugin.rankService().getNextRank(minutes);
        if (now != null) p.sendMessage(Chat.color(plugin.msg("prefix")+plugin.msg("rank_info.now").replace("{now}", now.display)));
        if (next != null) {
            int need = Math.max(0, next.requiredMinutes - minutes);
            p.sendMessage(Chat.color(plugin.msg("prefix")+plugin.msg("rank_info.next")
                .replace("{need_minutes}", String.valueOf(need))
                .replace("{next}", next.display)
                .replace("{group}", next.group == null ? "-" : next.group)));
        }
        plugin.rankService().checkAndApplyRank(p);
        return true;
    }
}

class cmd_TitleRoot implements CommandExecutor {
    private final Main plugin;
    private final TitleService titleService;
    private final TitleGUI titleGUI;
    public cmd_TitleRoot(Main p, TitleService ts, TitleGUI gui){ this.plugin=p; this.titleService=ts; this.titleGUI=gui; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
        if (args.length==0) {
            s.sendMessage(Chat.color("&f[칭호 도움말]"));
            s.sendMessage(Chat.color("&7/칭호 열기 &f- 내 칭호 GUI 열기"));
            s.sendMessage(Chat.color("&7/칭호 생성 <이름> <슬롯> &f- (OP) 칭호 토큰 생성"));
            s.sendMessage(Chat.color("&7/칭호 삭제 <슬롯> &f- (OP) 모든 유저에서 해당 슬롯 칭호 제거"));
            s.sendMessage(Chat.color("&7/칭호 삭제 <플레이어> <슬롯> &f- (OP) 해당 유저의 슬롯 칭호 제거"));
            s.sendMessage(Chat.color("&7/칭호 아이템 &f- (OP) 손에 든 아이템을 칭호 커스텀권으로 설정"));
            return true;
        }
        String sub = args[0];
        if (sub.equalsIgnoreCase("열기")) {
            if (!(s instanceof Player)) { s.sendMessage("플레이어만 사용"); return true; }
            titleGUI.open((Player)s);
            return true;
        }
        if (sub.equalsIgnoreCase("생성")) {
            if (!s.isOp()) { s.sendMessage("OP만 사용"); return true; }
            if (args.length < 3) { s.sendMessage("/칭호 생성 <이름> <슬롯>"); return true; }
            String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length-1));
            int slot;
            try { slot = Integer.parseInt(args[args.length-1]); } catch (Exception ex) { s.sendMessage("슬롯은 숫자"); return true; }
            if (!(s instanceof Player)) { s.sendMessage("플레이어가 아이템을 받아야 합니다."); return true; }
            Player p = (Player) s;
            ItemStack it = new ItemStack(Material.NAME_TAG);
            ItemMeta im = it.getItemMeta();
            im.setDisplayName(Chat.color("&d칭호 토큰: &f"+name));
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(Chat.color("&7우클릭하면 이 칭호가 등록/적용됩니다."));
            im.setLore(lore);
            im.getPersistentDataContainer().set(new NamespacedKey(plugin, "title_slot"), PersistentDataType.INTEGER, slot);
            im.getPersistentDataContainer().set(new NamespacedKey(plugin, "title_text"), PersistentDataType.STRING, name);
            it.setItemMeta(im);
            p.getInventory().addItem(it);
            s.sendMessage(Chat.color("&a칭호 토큰을 지급했습니다."));
            return true;
        }
        if (sub.equalsIgnoreCase("삭제")) {
            if (!s.isOp()) { s.sendMessage("OP만 사용"); return true; }
            if (args.length == 2) {
                int slot;
                try { slot = Integer.parseInt(args[1]); } catch (Exception ex) { s.sendMessage("/칭호 삭제 <슬롯> | /칭호 삭제 <플레이어> <슬롯>"); return true; }
                int cnt = plugin.storage().purgeSlotAllPlayers(slot);
                s.sendMessage(Chat.color("&a모든 유저에서 슬롯 "+slot+" 칭호를 제거했습니다. ("+cnt+"명)"));
                return true;
            } else if (args.length == 3) {
                org.bukkit.OfflinePlayer t = Bukkit.getOfflinePlayer(args[1]);
                if (t == null || t.getName() == null) { s.sendMessage("플레이어 없음"); return true; }
                int slot;
                try { slot = Integer.parseInt(args[2]); } catch (Exception ex) { s.sendMessage("/칭호 삭제 <플레이어> <슬롯>"); return true; }
                titleService.removeCustomTitle(t.getUniqueId(), slot);
                s.sendMessage(Chat.color("&a"+t.getName()+"의 슬롯 "+slot+" 칭호 제거"));
                return true;
            } else {
                s.sendMessage("/칭호 삭제 <슬롯> | /칭호 삭제 <플레이어> <슬롯>");
                return true;
            }
        }
        if (sub.equalsIgnoreCase("아이템")) {
            if (!s.isOp()) { s.sendMessage("OP만 사용"); return true; }
            if (!(s instanceof Player)) { s.sendMessage("플레이어만 사용"); return true; }
            Player p = (Player) s;
            ItemStack it = p.getInventory().getItemInMainHand();
            if (it == null || it.getType() == Material.AIR) { s.sendMessage("손에 아이템이 없습니다."); return true; }
            ItemMeta im = it.getItemMeta();
            String nameCfg = plugin.getConfig().getString("title_voucher.name", "&d칭호 커스텀권");
            java.util.List<String> loreCfg = plugin.getConfig().getStringList("title_voucher.lore");
            im.setDisplayName(Chat.color(nameCfg));
            java.util.List<String> lore = new java.util.ArrayList<>();
            for (String line : loreCfg) lore.add(Chat.color(line));
            im.setLore(lore);
            im.getPersistentDataContainer().set(new NamespacedKey(plugin, "title_voucher"), PersistentDataType.BYTE, (byte)1);
            it.setItemMeta(im);
            p.getInventory().setItemInMainHand(it);
            s.sendMessage(Chat.color("&a들고있는 아이템을 '칭호 커스텀권'으로 설정했습니다."));
            return true;
        }
        s.sendMessage("사용법: /칭호 [열기|생성 <이름> <슬롯>|삭제 <슬롯>|삭제 <플레이어> <슬롯>|아이템]");
        return true;
    }
}
