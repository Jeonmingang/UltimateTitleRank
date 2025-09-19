package com.minkang.ultimate.titles;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class TitleService {
    private final Main plugin;
    private final Map<Integer, String> generatedTitles = new HashMap<>();

    public TitleService(Main plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player p) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Inventory inv = Bukkit.createInventory(p, 27, Chat.color("&8칭호"));
            PlayerData d = plugin.storage().get(p.getUniqueId());
            for (Map.Entry<Integer,String> e : d.getCustomTitles().entrySet()) {
                ItemStack it = new ItemStack(Material.NAME_TAG);
                ItemMeta im = it.getItemMeta();
                im.setDisplayName(Chat.color("&f슬롯 " + e.getKey() + ": &r" + e.getValue()));
                it.setItemMeta(im);
                inv.addItem(it);
            }
            p.openInventory(inv);
        });
    }

    public void applyTitleSuffix(Player p, String suffixRaw, Integer slot) {
        boolean enabled = plugin.getConfig().getBoolean("title_suffix.enabled", true);
        int prio = plugin.getConfig().getInt("title_suffix.priority", 100);
        String clearCmd = plugin.getConfig().getString("title_suffix.clear_command", "lp user {player} meta removesuffix {priority}");
        String setCmd   = plugin.getConfig().getString("title_suffix.set_command",   "lp user {player} meta setsuffix {priority} {suffix}");
        String fmt      = plugin.getConfig().getString("title_suffix.format", "{title}");

        String suffix = suffixRaw == null ? "" : Chat.color(suffixRaw);
        String formatted = Chat.color(fmt.replace("{title}", suffix));
        String player = p.getName();

        if (enabled) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), clearCmd.replace("{player}", player).replace("{priority}", String.valueOf(prio)));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), setCmd.replace("{player}", player).replace("{priority}", String.valueOf(prio)).replace("{suffix}", formatted).replace(' ', '_'));
        }
        PlayerData d = plugin.storage().get(p.getUniqueId());
        d.setActiveTitleSlot(slot);
        plugin.storage().save(p.getUniqueId(), d);
    }

    public void setCustomTitle(java.util.UUID uuid, int slot, String title) {
        PlayerData d = plugin.storage().get(uuid);
        d.getCustomTitles().put(slot, title);
        d.setActiveTitleSlot(slot);
        plugin.storage().save(uuid, d);
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) applyTitleSuffix(p, title, slot);
    }

    public void removeCustomTitle(java.util.UUID uuid, int slot) {
        PlayerData d = plugin.storage().get(uuid);
        String removed = d.getCustomTitles().remove(slot);
        if (d.getActiveTitleSlot() != null && d.getActiveTitleSlot() == slot) {
            d.setActiveTitleSlot(null);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                boolean enabled = plugin.getConfig().getBoolean("title_suffix.enabled", true);
                int prio = plugin.getConfig().getInt("title_suffix.priority", 100);
                String clearCmd = plugin.getConfig().getString("title_suffix.clear_command", "lp user {player} meta removesuffix {priority}");
                if (enabled) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), clearCmd.replace("{player}", p.getName()).replace("{priority}", String.valueOf(prio)));
            }
        }
        plugin.storage().save(uuid, d);
    }
}
