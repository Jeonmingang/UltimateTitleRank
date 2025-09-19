package com.minkang.ultimate.titles;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TitleGUI {
    private final Main plugin;
    private final TitleService titleService;

    public TitleGUI(Main plugin, TitleService titleService) {
        this.plugin = plugin;
        this.titleService = titleService;
    }

    public void open(Player p) {
        PlayerData d = plugin.storage().get(p.getUniqueId());
        int size = Math.min(54, Math.max(9, ((d.getCustomTitles().size()+8)/9)*9));
        Inventory inv = Bukkit.createInventory(p, size, Chat.color("&8칭호"));
        for (Map.Entry<Integer,String> e : d.getCustomTitles().entrySet()) {
            ItemStack it = new ItemStack(Material.NAME_TAG);
            ItemMeta im = it.getItemMeta();
            im.setDisplayName(Chat.color("&f슬롯 " + e.getKey() + ": &r" + e.getValue()));
            List<String> lore = new ArrayList<>();
            lore.add(Chat.color("&7좌/우클릭: 적용"));
            im.setLore(lore);
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(im);
            inv.addItem(it);
        }
        p.openInventory(inv);
    }
}
