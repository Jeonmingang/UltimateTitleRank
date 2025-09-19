
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

public class TitleTokenListener implements Listener {
    private final Main plugin;
    private final org.bukkit.NamespacedKey keySlot;
    private final org.bukkit.NamespacedKey keyTitle;
    private final org.bukkit.NamespacedKey keyVoucher;

    public TitleTokenListener(Main plugin) {
        this.plugin = plugin;
        this.keySlot = new org.bukkit.NamespacedKey(plugin, "title_slot");
        this.keyTitle = new org.bukkit.NamespacedKey(plugin, "title_text");
        this.keyVoucher = new org.bukkit.NamespacedKey(plugin, "title_voucher");
    }

    private boolean matchesVoucherMeta(org.bukkit.inventory.ItemStack it) {
        if (it == null || !it.hasItemMeta()) return false;
        org.bukkit.inventory.meta.ItemMeta im = it.getItemMeta();
        // PDC 우선
        if (im.getPersistentDataContainer().has(keyVoucher, org.bukkit.persistence.PersistentDataType.BYTE)) return true;
        // name/lore 매칭
        String nameCfg = plugin.getConfig().getString("title_voucher.name", "&d칭호 커스텀권");
        java.util.List<String> loreCfg = plugin.getConfig().getStringList("title_voucher.lore");
        String name = im.hasDisplayName() ? im.getDisplayName() : "";
        if (!org.bukkit.ChatColor.stripColor(name).equals(org.bukkit.ChatColor.stripColor(Chat.color(nameCfg)))) return false;
        java.util.List<String> lore = im.hasLore() ? im.getLore() : java.util.Collections.emptyList();
        if (loreCfg.isEmpty()) return true; // 이름만 맞으면 패스
        if (lore == null || lore.size() < loreCfg.size()) return false;
        for (int i = 0; i < loreCfg.size(); i++) {
            String a = org.bukkit.ChatColor.stripColor(lore.get(i));
            String b = org.bukkit.ChatColor.stripColor(Chat.color(loreCfg.get(i)));
            if (!a.equals(b)) return false;
        }
        return true;
    }

    @org.bukkit.event.EventHandler
    public void onUse(org.bukkit.event.player.PlayerInteractEvent e) {
        if (e.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        org.bukkit.inventory.ItemStack it = e.getItem();
        if (it == null || !it.hasItemMeta()) return;
        org.bukkit.inventory.meta.ItemMeta im = it.getItemMeta();
        org.bukkit.entity.Player p = e.getPlayer();

        // 1) 고정 칭호 토큰 (slot/title 내장형)
        if (im.getPersistentDataContainer().has(keySlot, org.bukkit.persistence.PersistentDataType.INTEGER)) {
            e.setCancelled(true);
            Integer slot = im.getPersistentDataContainer().get(keySlot, org.bukkit.persistence.PersistentDataType.INTEGER);
            String title = im.getPersistentDataContainer().getOrDefault(keyTitle, org.bukkit.persistence.PersistentDataType.STRING, "");
            if (slot != null) {
                plugin.titles().setCustomTitle(p.getUniqueId(), slot, title);
                plugin.titles().applyTitleSuffix(p, title, slot);
                p.sendMessage(Chat.color(plugin.msg("prefix") + "&a칭호가 등록/적용되었습니다: &f" + title));
                it.setAmount(Math.max(0, it.getAmount()-1));
                p.getInventory().setItemInMainHand(it.getAmount()>0?it:null);
            }
            return;
        }

        // 2) 커스텀권 (이름/로어 또는 PDC로 식별)
        if (matchesVoucherMeta(it)) {
            e.setCancelled(true);
            // 아이템 1개 소모
            it.setAmount(Math.max(0, it.getAmount()-1));
            p.getInventory().setItemInMainHand(it.getAmount()>0?it:null);
            // 채팅 대기 등록
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
