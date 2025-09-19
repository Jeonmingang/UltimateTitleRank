
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
    private final NamespacedKey keySlot;
    private final NamespacedKey keyTitle;

    public TitleTokenListener(Main plugin) {
        this.plugin = plugin;
        this.keySlot = new NamespacedKey(plugin, "title_slot");
        this.keyTitle = new NamespacedKey(plugin, "title_text");
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        ItemStack it = e.getItem();
        if (it == null || !it.hasItemMeta()) return;
        ItemMeta im = it.getItemMeta();
        if (!im.getPersistentDataContainer().has(keySlot, PersistentDataType.INTEGER)) return;
        e.setCancelled(true);
        Integer slot = im.getPersistentDataContainer().get(keySlot, PersistentDataType.INTEGER);
        String title = im.getPersistentDataContainer().getOrDefault(keyTitle, PersistentDataType.STRING, "");
        Player p = e.getPlayer();
        if (slot == null) return;
        plugin.titles().setCustomTitle(p.getUniqueId(), slot, title);
        plugin.titles().applyTitleSuffix(p, title);
        p.sendMessage(Chat.color(plugin.msg("prefix") + "&a칭호가 등록/적용되었습니다: &f" + title));
        // consume item
        it.setAmount(Math.max(0, it.getAmount()-1));
        p.getInventory().setItemInMainHand(it.getAmount()>0?it:null);
    }
}
