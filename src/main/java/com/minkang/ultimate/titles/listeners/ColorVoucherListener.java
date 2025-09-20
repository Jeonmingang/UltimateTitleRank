package com.minkang.ultimate.titles.listeners;

import com.minkang.ultimate.titles.UltimateTitleRank;
import com.minkang.ultimate.titles.storage.PlayerData;
import com.minkang.ultimate.titles.util.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.block.Action;

import java.util.List;

public class ColorVoucherListener implements Listener {
    private final UltimateTitleRank plugin;
    public ColorVoucherListener(UltimateTitleRank plugin){ this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUse(PlayerInteractEvent e){
        // only right-click
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        // only main hand to avoid double fire
        if (e.getHand() != null && e.getHand() == EquipmentSlot.OFF_HAND) return;

        ItemStack it = e.getItem();
        if (it == null || it.getType() == Material.AIR) return;
        ItemMeta im = it.getItemMeta();
        if (im == null || !im.hasLore()) return;
        List<String> lore = im.getLore();
        if (lore == null) return;

        String key = null;
        for (String line : lore){
            String plain = ChatColor.stripColor(line == null ? "" : line);
            if (plain.startsWith("KEY:")){
                key = plain.substring(4).trim();
                break;
            }
        }
        if (key == null || key.isEmpty()) return;

        Player p = e.getPlayer();
        PlayerData pd = plugin.getStorage().get(p.getUniqueId());
        if (!pd.getColorPermissions().contains(key)){
            pd.getColorPermissions().add(key);
        }
        // consume one
        int amt = it.getAmount();
        if (amt <= 1){
            if (e.getHand() == EquipmentSlot.HAND) {
                p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            } else {
                p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            }
        } else {
            it.setAmount(amt - 1);
        }
        p.sendMessage(ColorUtil.colorize("&a색깔채팅 권한을 획득했습니다: &f" + key));
        e.setCancelled(true);
    }
}
