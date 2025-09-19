
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
        int size = plugin.getConfig().getInt("gui.size", 54);
        String title = Chat.color(plugin.getConfig().getString("gui.title", "&8Titles"));
        Inventory inv = Bukkit.createInventory(p, size, title);

        PlayerData data = plugin.storage().get(p.getUniqueId());

        for (Map.Entry<Integer,String> e : titleService.getGeneratedTitles().entrySet()) {
            int slot = e.getKey();
            if (slot < 0 || slot >= size) continue;
            inv.setItem(slot, makeItem(Material.NAME_TAG, e.getValue(), "&7우클릭: 적용"));
        }
        for (Map.Entry<Integer,String> e : data.getCustomTitles().entrySet()) {
            int slot = e.getKey();
            if (slot < 0 || slot >= size) continue;
            inv.setItem(slot, makeItem(Material.BOOK, e.getValue(), "&7우클릭: 적용(커스텀)"));
        }

        p.openInventory(inv);
        p.sendMessage(Chat.color(plugin.msg("prefix") + plugin.msg("open_gui")));
    }

    private ItemStack makeItem(Material m, String name, String loreLine) {
        ItemStack it = new ItemStack(m);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(Chat.color(name));
        List<String> lore = new ArrayList<>();
        lore.add(Chat.color(loreLine));
        im.setLore(lore);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(im);
        return it;
    }
}
