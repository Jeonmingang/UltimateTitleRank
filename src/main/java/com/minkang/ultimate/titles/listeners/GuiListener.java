package com.minkang.ultimate.titles.listeners;

import com.minkang.ultimate.titles.UltimateTitleRank;
import com.minkang.ultimate.titles.commands.TitleCommand;
import com.minkang.ultimate.titles.util.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiListener implements Listener {
    private final UltimateTitleRank plugin;
    public GuiListener(UltimateTitleRank plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();
        String titleGui = ColorUtil.colorize(plugin.getConfig().getString("title_gui.title", "&8칭호 선택"));
        String colorGui = ColorUtil.colorize(plugin.getConfig().getString("colorchat.gui_title", "&8색깔채팅 선택"));
        if (title.equals(titleGui)) {
            e.setCancelled(true);
            ItemStack it = e.getCurrentItem();
            if (it == null || it.getType() == Material.AIR) return;
            ItemMeta meta = it.getItemMeta();
            if (meta == null) return;
            String display = meta.getDisplayName();
            if (display == null) return;
            com.minkang.ultimate.titles.storage.PlayerData pd = plugin.getStorage().get(p.getUniqueId());
            pd.setSelectedTitle(display);
            p.sendMessage(ColorUtil.colorize("&a선택된 칭호: &f") + display);
            p.closeInventory();
            return;
        }
        if (title.equals(colorGui)) {
            e.setCancelled(true);
            ItemStack it = e.getCurrentItem();
            if (it == null || it.getType() == Material.AIR) return;
            if (it.getType() == Material.BARRIER) {
                com.minkang.ultimate.titles.storage.PlayerData pd = plugin.getStorage().get(p.getUniqueId());
                pd.setSelectedColor("WHITE");
                p.sendMessage(ColorUtil.colorize("&a채팅 색이 기본값으로 초기화되었습니다."));
                p.closeInventory();
                return;
            }
            String key = TitleCommand.decodeHiddenKey(it);
            if (key != null && !key.isEmpty()) {
                com.minkang.ultimate.titles.storage.PlayerData pd = plugin.getStorage().get(p.getUniqueId());
                if (pd.getColorPermissions().contains(key)) {
                    pd.setSelectedColor(key);
                    p.sendMessage(ColorUtil.colorize("&a채팅 색이 설정되었습니다: &f" + key));
                    p.closeInventory();
                } else {
                    p.sendMessage(ColorUtil.colorize("&c해당 색 권한이 없습니다. /색깔채팅 지급 <색깔> <플레이어>"));
                }
            }
            return;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        // no-op
    }
}
