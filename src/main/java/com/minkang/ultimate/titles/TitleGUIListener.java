
package com.minkang.ultimate.titles;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TitleGUIListener implements Listener {
    private final Main plugin;
    private final TitleService titleService;

    public TitleGUIListener(Main plugin, TitleService s) {
        this.plugin = plugin;
        this.titleService = s;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player)e.getWhoClicked();
        String guiTitle = Chat.color(plugin.getConfig().getString("gui.title", "&8칭호 선택"));
        if (!e.getView().getTitle().equals(Chat.color(guiTitle))) return;
        e.setCancelled(true);
        ItemStack it = e.getCurrentItem();
        if (it == null || !it.hasItemMeta()) return;
        ItemMeta im = it.getItemMeta();
        if (!im.hasDisplayName()) return;
        String title = im.getDisplayName();
        titleService.applyTitle(p, title);
    }
}
