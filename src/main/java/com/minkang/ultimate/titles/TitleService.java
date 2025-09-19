
package com.minkang.ultimate.titles;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TitleService {
    private final Main plugin;
    private final Map<Integer, String> generatedTitles = new HashMap<>();

    public TitleService(Main plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        TitlesFile.reload(plugin);
        generatedTitles.clear();
        generatedTitles.putAll(TitlesFile.getAll());
    }

    public Map<Integer, String> getGeneratedTitles() { return Collections.unmodifiableMap(generatedTitles); }

    public void createTitle(int slot, String name) {
        generatedTitles.put(slot, name);
        TitlesFile.set(slot, name);
        TitlesFile.save();
    }

    public void deleteTitle(int slot) {
        generatedTitles.remove(slot);
        TitlesFile.remove(slot);
        TitlesFile.save();
    }

    public void applyTitle(Player p, String rawTitle) {
        String colored = Chat.color(rawTitle);
        plugin.luck().applyRank(p, new Rank("custom", colored, 0, null, colored, 0, 0));
        p.sendMessage(Chat.color(plugin.msg("prefix") + plugin.msg("apply.success").replace("{title}", colored)));
    }
}


public void applyTitleSuffix(org.bukkit.entity.Player p, String suffixRaw) {
    if (suffixRaw == null) suffixRaw = "";
    String suffix = Chat.color(suffixRaw);
    org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(),
            "lp user " + p.getName() + " meta removesuffix 100");
    org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(),
            "lp user " + p.getName() + " meta setsuffix 100 " + suffix.replace(' ', '_'));
}

public void setCustomTitle(java.util.UUID uuid, int slot, String title) {
    PlayerData d = plugin.storage().get(uuid);
    d.getCustomTitles().put(slot, title);
    plugin.storage().save(uuid, d);
}

public void removeCustomTitle(java.util.UUID uuid, int slot) {
    PlayerData d = plugin.storage().get(uuid);
    d.getCustomTitles().remove(slot);
    plugin.storage().save(uuid, d);
}

public void openGUI(org.bukkit.entity.Player p) {
    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(p, 27, Chat.color("&8칭호"));
        PlayerData d = plugin.storage().get(p.getUniqueId());
        for (java.util.Map.Entry<Integer,String> e : d.getCustomTitles().entrySet()) {
            org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NAME_TAG);
            org.bukkit.inventory.meta.ItemMeta im = it.getItemMeta();
            im.setDisplayName(Chat.color("&f슬롯 "+e.getKey()+": &r"+e.getValue()));
            it.setItemMeta(im);
            inv.addItem(it);
        }
        p.openInventory(inv);
    });
}
