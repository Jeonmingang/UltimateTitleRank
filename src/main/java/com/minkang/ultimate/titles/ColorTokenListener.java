package com.minkang.ultimate.titles;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ColorTokenListener implements Listener {
    private final Main plugin;
    private final ColorChatService service;

    public ColorTokenListener(Main plugin, ColorChatService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        ItemStack it = e.getItem();
        if (it == null || !it.hasItemMeta()) return;
        ItemMeta im = it.getItemMeta();
        String name = im.getDisplayName();
        for (ColorDef d : service.all()) {
            if (name != null && name.contains(d.name)) {
                e.setCancelled(true);
                service.grant(e.getPlayer().getUniqueId(), d.id);
                e.getPlayer().sendMessage(Chat.color(plugin.msg("prefix") + "&a색깔채팅 획득: " + d.code + d.name));
                it.setAmount(Math.max(0, it.getAmount()-1));
                Player p = e.getPlayer();
                p.getInventory().setItemInMainHand(it.getAmount()>0?it:null);
                return;
            }
        }
    }
}
