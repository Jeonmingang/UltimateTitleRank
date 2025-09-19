
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


public void applyTitleSuffix(org.bukkit.entity.Player p, String suffixRaw, Integer slot) {
    boolean enabled = plugin.getConfig().getBoolean("title_suffix.enabled", true);
    int prio = plugin.getConfig().getInt("title_suffix.priority", 100);
    String clearCmd = plugin.getConfig().getString("title_suffix.clear_command", "lp user {player} meta removesuffix {priority}");
    String setCmd   = plugin.getConfig().getString("title_suffix.set_command",   "lp user {player} meta setsuffix {priority} {suffix}");
    String suffix = suffixRaw == null ? "" : Chat.color(suffixRaw);
    String fmt = plugin.getConfig().getString("title_suffix.format", "{title}");
    String formatted = Chat.color(fmt.replace("{title}", suffix));
    String player = p.getName();
    if (enabled) {
        org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(),
                clearCmd.replace("{player}", player).replace("{priority}", String.valueOf(prio)));
        org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(),
                setCmd.replace("{player}", player).replace("{priority}", String.valueOf(prio)).replace("{suffix}", formatted).replace(' ', '_'));
    }
    // track active slot
    PlayerData d = plugin.storage().get(p.getUniqueId());
    d.setActiveTitleSlot(slot);
    plugin.storage().save(p.getUniqueId(), d);
}
public void setCustomTitle(java.util.UUID uuid, int slot, String title) {
    PlayerData d = plugin.storage().get(uuid);
    d.getCustomTitles().put(slot, title);
    d.setActiveTitleSlot(slot);
    plugin.storage().save(uuid, d);
    org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(uuid);
    if (p != null) applyTitleSuffix(p, title, slot);
}
public void removeCustomTitle(java.util.UUID uuid, int slot) {
    PlayerData d = plugin.storage().get(uuid);
    String removed = d.getCustomTitles().remove(slot);
    if (d.getActiveTitleSlot() != null && d.getActiveTitleSlot() == slot) {
        d.setActiveTitleSlot(null);
        org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(uuid);
        if (p != null) {
            boolean enabled = plugin.getConfig().getBoolean("title_suffix.enabled", true);
            int prio = plugin.getConfig().getInt("title_suffix.priority", 100);
            String clearCmd = plugin.getConfig().getString("title_suffix.clear_command", "lp user {player} meta removesuffix {priority}");
            if (enabled) org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(),
                    clearCmd.replace("{player}", p.getName()).replace("{priority}", String.valueOf(prio)));
        }
    }
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
