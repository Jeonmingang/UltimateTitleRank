
package com.minkang.ultimate.titles;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CustomTitleItemListener implements Listener {
    private final Main plugin;
    private final TitleService titleService;
    private final Set<UUID> awaiting = new HashSet<>();

    public CustomTitleItemListener(Main plugin, TitleService s) {
        this.plugin = plugin;
        this.titleService = s;
    }

    private boolean isCustomTitleItem(ItemStack it) {
        if (it == null || it.getType() == Material.AIR) return false;
        if (!it.hasItemMeta()) return false;
        ItemMeta im = it.getItemMeta();
        String name = Chat.color(plugin.getConfig().getString("custom_title_item.name"));
        if (!im.hasDisplayName() || !Chat.color(im.getDisplayName()).equals(name)) return false;
        List<String> lore = im.getLore();
        List<String> cfg = plugin.getConfig().getStringList("custom_title_item.lore_lines");
        if (lore == null || lore.size() < cfg.size()) return false;
        for (int i=0;i<cfg.size();i++) {
            if (!Chat.color(lore.get(i)).equals(Chat.color(cfg.get(i)))) return false;
        }
        return true;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        ItemStack it = e.getItem();
        if (!isCustomTitleItem(it)) return;
        e.setCancelled(true);
        Player p = e.getPlayer();
        awaiting.add(p.getUniqueId());
        p.sendMessage(Chat.color(Main.get().msg("prefix") + Main.get().msg("custom.prompt")));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!awaiting.contains(p.getUniqueId())) return;
        e.setCancelled(true);
        String msg = e.getMessage();
        if (msg.equalsIgnoreCase("취소")) {
            awaiting.remove(p.getUniqueId());
            p.sendMessage(Chat.color(Main.get().msg("prefix") + Main.get().msg("custom.cancelled")));
            return;
        }
        PlayerData data = Main.get().storage().get(p.getUniqueId());
        int slot = findEmptySlot(data);
        data.getCustomTitles().put(slot, msg);
        Main.get().storage().save(p.getUniqueId(), data);
        awaiting.remove(p.getUniqueId());
        p.sendMessage(Chat.color(Main.get().msg("prefix") + Main.get().msg("custom.set")));
    }

    private int findEmptySlot(PlayerData d) {
        for (int i=0;i<54;i++) {
            if (!d.getCustomTitles().containsKey(i) && !d.getFixedTitles().containsKey(i)) return i;
        }
        return 0;
    }
}
