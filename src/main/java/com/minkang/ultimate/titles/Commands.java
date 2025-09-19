
package com.minkang.ultimate.titles;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
                .replace("{next}", next.display)
                .replace("{need_h}", String.valueOf(need/60))
                .replace("{need_m}", String.valueOf(need%60))));
        } else {
            p.sendMessage(Chat.color(plugin.msg("prefix")+plugin.msg("rank_info.max")));
        }
        PlayerData d = plugin.storage().get(p.getUniqueId());
        Rank r = now;
        if (r != null) {
            p.sendMessage(Chat.color(plugin.msg("prefix")+plugin.msg("limits.used")
                .replace("{leftL}", String.valueOf(Math.max(0, r.limitLegendary - d.getDailyLegendaryUsed())))
                .replace("{leftM}", String.valueOf(Math.max(0, r.limitMegaboss - d.getDailyMegabossUsed())))));
        }
        return true;
    }
}

class cmd_Title implements CommandExecutor {
    private final Main plugin;
    private final TitleGUI gui;
    public cmd_Title(Main p, TitleGUI g){ this.plugin=p; this.gui=g; }
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player)) { s.sendMessage(plugin.msg("not_player")); return true; }
        Player p=(Player)s;
        gui.open(p);
        return true;
    }
}

class cmd_AdminCreate implements CommandExecutor {
    private final Main plugin;
    public cmd_AdminCreate(Main p){ this.plugin=p; }
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!s.isOp()) { s.sendMessage("OP 전용."); return true; }
        if (a.length < 2) { s.sendMessage("/칭호생성 <이름> <슬롯>"); return true; }
        String name = a[0];
        int slot;
        try { slot = Integer.parseInt(a[1]); } catch(Exception ex){ s.sendMessage(plugin.msg("errors.number")); return true; }
        plugin.titleService().createTitle(slot, name);
        s.sendMessage(Chat.color(plugin.msg("prefix")+plugin.msg("admin.created").replace("{slot}", String.valueOf(slot)).replace("{name}", name)));
        return true;
    }
}

class cmd_AdminDelete implements CommandExecutor {
    private final Main plugin;
    public cmd_AdminDelete(Main p){ this.plugin=p; }
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!s.isOp()) { s.sendMessage("OP 전용."); return true; }
        if (a.length < 2) { s.sendMessage("/칭호삭제 <플레이어> <슬롯>"); return true; }
        Player t = Bukkit.getPlayerExact(a[0]);
        if (t == null) { s.sendMessage("플레이어 오프라인"); return true; }
        int slot;
        try { slot = Integer.parseInt(a[1]); } catch(Exception ex){ s.sendMessage(plugin.msg("errors.number")); return true; }
        PlayerData d = plugin.storage().get(t.getUniqueId());
        d.getCustomTitles().remove(slot);
        d.getFixedTitles().remove(slot);
        plugin.storage().save(t.getUniqueId(), d);
        s.sendMessage(Chat.color(plugin.msg("prefix")+plugin.msg("admin.removed")
                .replace("{player}", t.getName()).replace("{slot}", String.valueOf(slot))));
        return true;
    }
}

class cmd_AdminList implements CommandExecutor {
    private final Main plugin;
    public cmd_AdminList(Main p){ this.plugin=p; }
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!s.isOp()) { s.sendMessage("OP 전용."); return true; }
        s.sendMessage(Chat.color(plugin.msg("prefix")+plugin.msg("admin.list_header")));
        for (java.util.Map.Entry<Integer,String> e : plugin.titleService().getGeneratedTitles().entrySet()) {
            s.sendMessage(Chat.color(plugin.msg("admin.list_line").replace("{slot}", String.valueOf(e.getKey())).replace("{name}", e.getValue())));
        }
        return true;
    }
}

class cmd_AdminItem implements CommandExecutor {
    private final Main plugin;
    public cmd_AdminItem(Main p){ this.plugin=p; }
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player)) { s.sendMessage(plugin.msg("not_player")); return true; }
        if (!s.isOp()) { s.sendMessage("OP 전용."); return true; }
        Player p=(Player)s;
        ItemStack it = p.getInventory().getItemInMainHand();
        if (it == null || it.getType() == Material.AIR) { p.sendMessage(plugin.msg("errors.hold_item")); return true; }
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(Chat.color(plugin.getConfig().getString("custom_title_item.name")));
        java.util.List<String> lore = new java.util.ArrayList<>();
        for (String line : plugin.getConfig().getStringList("custom_title_item.lore_lines")) lore.add(Chat.color(line));
        im.setLore(lore);
        it.setItemMeta(im);
        p.getInventory().setItemInMainHand(it);
        p.sendMessage(Chat.color("&a해당 아이템이 칭호 커스텀권으로 설정되었습니다."));
        return true;
    }
}
