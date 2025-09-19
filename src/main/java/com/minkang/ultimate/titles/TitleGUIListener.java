package com.minkang.ultimate.titles;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TitleGUIListener implements Listener {
    private final Main plugin;
    private final TitleService titleService;

    public TitleGUIListener(Main plugin, TitleService titleService) {
        this.plugin = plugin;
        this.titleService = titleService;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player)) return;
        Player p = (Player) he;
        if (e.getView().getTitle() == null) return;
        if (!Chat.color("&8칭호").equals(e.getView().getTitle())) return;
        e.setCancelled(true);
        ItemStack it = e.getCurrentItem();
        if (it == null || it.getType() == Material.AIR) return;
        ItemMeta im = it.getItemMeta();
        if (im == null || !im.hasDisplayName()) return;
        String name = im.getDisplayName();
        int idx = name.indexOf(": ");
        if (idx >= 0 && idx+2 < name.length()) {
            String title = name.substring(idx+2);
            titleService.applyTitle(p, title);
            p.sendMessage(Chat.color(plugin.msg("prefix") + "&a칭호 적용: &f" + title));
            p.closeInventory();
        }
    }
}
