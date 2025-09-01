
package com.minkang.ultimate.listeners;

import com.minkang.ultimate.UltimateTitleRank;
import com.minkang.ultimate.util.Texts;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BookListener implements Listener {

    private final UltimateTitleRank plugin;

    public BookListener(UltimateTitleRank plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUseBook(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack inHand = e.getItem();
        String title = plugin.getTitleManager().parseTitleFromItem(inHand);
        if (title == null) return;
        Player p = e.getPlayer();

        plugin.getTitleManager().addOwnedTitle(p.getUniqueId(), title);
        plugin.getTitleManager().setSelectedTitle(p.getUniqueId(), title);
        p.sendMessage(Texts.color("&a칭호를 획득하고 적용했습니다: &f" + title));
        // consume
        if (inHand != null) {
            int amt = inHand.getAmount();
            if (amt <= 1) {
                p.getInventory().setItemInMainHand(null);
            } else {
                inHand.setAmount(amt - 1);
            }
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onClickGUI(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        String invName = e.getView().getTitle();
        String expected = Texts.color(plugin.getConfig().getString("gui.title", "&8보유한 칭호 선택"));
        if (!invName.equals(expected)) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;
        String display = clicked.getItemMeta().getDisplayName();
        if (display == null || display.trim().isEmpty()) return;

        // strip color
        String raw = display.replaceAll("§[0-9a-fk-or]", "");
        plugin.getTitleManager().setSelectedTitle(p.getUniqueId(), raw);
        p.sendMessage(Texts.color("&a칭호 적용: &f" + raw));
        p.closeInventory();
    }
}
