package com.minkang.ultimate.titles;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ColorChatGUI {
    private final Main plugin;
    private final ColorChatService service;

    public ColorChatGUI(Main plugin, ColorChatService service) {
        this.plugin = plugin;
        this.service = service;
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(p, 9, Chat.color("&8색깔채팅"));
        for (ColorDef d : service.all()) {
            ItemStack it = new ItemStack(d.material);
            ItemMeta im = it.getItemMeta();
            im.setDisplayName(Chat.color(d.code + d.name));
            it.setItemMeta(im);
            inv.addItem(it);
        }
        p.openInventory(inv);
    }
}
