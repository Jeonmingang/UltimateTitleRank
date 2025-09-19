package com.minkang.ultimate.titles;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class TitleTokenListener implements Listener {
    private final Main plugin;
    private final NamespacedKey keySlot;
    private final NamespacedKey keyTitle;
    private final NamespacedKey keyVoucher;

    public TitleTokenListener(Main plugin) {
        this.plugin = plugin;
        this.keySlot = new NamespacedKey(plugin, "title_slot");
        this.keyTitle = new NamespacedKey(plugin, "title_text");
        this.keyVoucher = new NamespacedKey(plugin, "title_voucher");
    }

    private boolean matchesVoucherMeta(ItemStack it) {
        if (it == null || !it.hasItemMeta()) return false;
        ItemMeta im = it.getItemMeta();
        if (im.getPersistentDataContainer().has(keyVoucher, PersistentDataType.BYTE)) return true;
        String nameCfg = plugin.getConfig().getString("title_voucher.name", "&d칭호 커스텀권");
        List<String> loreCfg = plugin.getConfig().getStringList("title_voucher.lore");
        String name = im.hasDisplayName() ? im.getDisplayName() : "";
        if (!org.bukkit.ChatColor.stripColor(name).equals(org.bukkit.ChatColor.stripColor(Chat.color(nameCfg)))) return false;
        List<String> lore = im.hasLore() ? im.getLore() : java.util.Collections.emptyList();
        if (loreCfg.isEmpty()) return true;
        if (lore == null || lore.size() < loreCfg.size()) return false;
        for (int i = 0; i < loreCfg.size(); i++) {
            String a = org.bukkit.ChatColor.stripColor(lore.get(i));
            String b = org.bukkit.ChatColor.stripColor(Chat.color(loreCfg.get(i)));
            if (!a.equals(b)) return false;
        }
        return true;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        ItemStack it = e.getItem();
        if (it == null || !it.hasItemMeta()) return;
        ItemMeta im = it.getItemMeta();
        Player p = e.getPlayer();

        if (im.getPersistentDataContainer().has(keySlot, PersistentDataType.INTEGER)) {
            e.setCancelled(true);
            Integer slot = im.getPersistentDataContainer().get(keySlot, PersistentDataType.INTEGER);
            String title = im.getPersistentDataContainer().getOrDefault(keyTitle, PersistentDataType.STRING, "");
            if (slot != null) {
                plugin.titles().setCustomTitle(p.getUniqueId(), slot, title);
                plugin.titles().applyTitleSuffix(p, title, slot);
                p.sendMessage(Chat.color(plugin.msg("prefix") + "&a칭호가 등록/적용되었습니다: &f" + title));
                it.setAmount(Math.max(0, it.getAmount()-1));
                p.getInventory().setItemInMainHand(it.getAmount()>0?it:null);
            }
            return;
        }

        if (matchesVoucherMeta(it)) {
            e.setCancelled(true);
            it.setAmount(Math.max(0, it.getAmount()-1));
            p.getInventory().setItemInMainHand(it.getAmount()>0?it:null);
            TitleChatInputListener l = plugin.getTitleChatInputListener();
            if (l != null) {
                l.await(p);
                p.sendMessage(Chat.color(plugin.msg("prefix") + "&e채팅에 원하는 칭호를 입력하세요 (& 색코드 지원). &7(30초 이내)"));
            } else {
                p.sendMessage(Chat.color(plugin.msg("prefix") + "&c내부 오류: 입력 리스너 없음"));
            }
        }
    }
}
