package com.minkang.ultimate.titles;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ColorChatGUIListener implements Listener {
    private final Main plugin;
    private final ColorChatService service;

    public ColorChatGUIListener(Main plugin, ColorChatService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        if (!Chat.color("&8색깔채팅").equals(e.getView().getTitle())) return;
        e.setCancelled(true);
        ItemStack it = e.getCurrentItem();
        if (it == null || it.getType() == Material.AIR) return;
        ItemMeta im = it.getItemMeta();
        if (im == null || !im.hasDisplayName()) return;
        String name = im.getDisplayName();
        for (ColorDef d : service.all()) {
            if (name.contains(d.name)) {
                service.setActive(p.getUniqueId(), d.id);
                p.sendMessage(Chat.color(plugin.msg("prefix") + "&a채팅색 적용: " + d.code + d.name));
                p.closeInventory();
                return;
            }
        }
    }
}
