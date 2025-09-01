
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
        Player p = e.getPlayer();
        // voucher?
        if (plugin.getTitleManager().isVoucher(inHand)) {
            int sec = plugin.getConfig().getInt("custom_title_voucher.input_timeout_seconds",30);
            String prompt = plugin.getConfig().getString("custom_title_voucher.prompt_on_use", "&e이제 채팅에 원하는 칭호를 입력하세요. &7(%sec%s 내 미입력시 취소)");
            prompt = prompt.replace("%sec%", String.valueOf(sec));
            plugin.getTitleManager().beginAwaiting(p.getUniqueId());
            p.sendMessage(Texts.color(prompt));
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
            return;
        }
        String title = plugin.getTitleManager().parseTitleFromItem(inHand);
        if (title == null) return;
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
        org.bukkit.persistence.PersistentDataContainer pdc = clicked.getItemMeta().getPersistentDataContainer();
        String raw = pdc.get(new org.bukkit.NamespacedKey(plugin, "title_name"), org.bukkit.persistence.PersistentDataType.STRING);
        if (raw == null || raw.trim().isEmpty()) {
            String display = clicked.getItemMeta().getDisplayName();
            if (display == null || display.trim().isEmpty()) return;
            raw = display.replaceAll("§[0-9a-fk-or]", "");
        }
        plugin.getTitleManager().setSelectedTitle(p.getUniqueId(), raw);
        p.sendMessage(Texts.color("&a칭호 적용: &f" + raw));
        p.closeInventory();
    }
}
