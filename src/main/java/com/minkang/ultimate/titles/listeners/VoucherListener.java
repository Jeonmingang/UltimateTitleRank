package com.minkang.ultimate.titles.listeners;

import com.minkang.ultimate.titles.UltimateTitleRank;
import com.minkang.ultimate.titles.storage.PlayerData;
import com.minkang.ultimate.titles.util.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VoucherListener implements Listener {
    private final UltimateTitleRank plugin;
    private final Set<UUID> waitingForTitle = new HashSet<UUID>();

    public VoucherListener(UltimateTitleRank plugin) { this.plugin = plugin; }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        ItemStack it = p.getInventory().getItemInMainHand();
        if (it == null || it.getType() == Material.AIR) return;
        ItemMeta meta = it.getItemMeta();
        if (meta == null) return;

        // 1) Title custom voucher
        String needName = plugin.getConfig().getString("custom_title_voucher.name", "&d칭호 커스텀권");
        java.util.List<String> needLore = plugin.getConfig().getStringList("custom_title_voucher.lore");
        String dn = meta.hasDisplayName() ? meta.getDisplayName() : "";
        java.util.List<String> lore = meta.hasLore() ? meta.getLore() : java.util.Collections.<String>emptyList();

        if (ChatColor.stripColor(dn).equals(ChatColor.stripColor(ColorUtil.colorize(needName)))) {
            if (needLore.size() == 0 || (lore.size() > 0 && ChatColor.stripColor(lore.get(0)).equals(ChatColor.stripColor(ColorUtil.colorize(needLore.get(0)))))) {
                consumeOne(p, it);
                waitingForTitle.add(p.getUniqueId());
                p.sendMessage(ColorUtil.colorize("&d원하는 칭호를 채팅으로 입력하세요. (& 코드 사용 가능)"));
                e.setCancelled(true);
                return;
            }
        }

        // 2) Color voucher (detect by lore KEY:<COLOR> and display starts with template prefix)
        String colorKey = extractKeyFromLore(lore, "KEY:");
        if (colorKey != null && !colorKey.isEmpty()) {
            // Heuristic: color voucher uses configured template "색깔채팅 권:" within display name
            String template = plugin.getConfig().getString("colorchat.voucher.name_template", "&b색깔채팅 권: {name}");
            String plainPrefix = ChatColor.stripColor(ColorUtil.colorize(template.replace("{name}", ""))).trim();
            if (ChatColor.stripColor(dn).startsWith(plainPrefix.replace(":", "").trim())) {
                PlayerData pd = plugin.getStorage().get(p.getUniqueId());
                if (!pd.getColorPermissions().contains(colorKey)) pd.getColorPermissions().add(colorKey);
                p.sendMessage(ColorUtil.colorize("&a색깔채팅 권한을 획득했습니다: &f" + colorKey));
                consumeOne(p, it);
                e.setCancelled(true);
            }
        }
    }

    private void consumeOne(Player p, ItemStack it) {
        int amt = it.getAmount();
        if (amt <= 1) p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        else it.setAmount(amt - 1);
    }

    private String extractKeyFromLore(java.util.List<String> lore, String prefix) {
        if (lore == null) return "";
        for (String l : lore) {
            String s = ChatColor.stripColor(l);
            if (s != null && s.startsWith(prefix)) {
                return s.substring(prefix.length());
            }
        }
        return "";
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (!waitingForTitle.contains(e.getPlayer().getUniqueId())) return;
        e.setCancelled(true);
        Player p = e.getPlayer();
        waitingForTitle.remove(p.getUniqueId());
        String raw = e.getMessage();
        String colored = ColorUtil.colorize(raw);
        PlayerData pd = plugin.getStorage().get(p.getUniqueId());
        pd.getOwnedTitles().add(colored);
        pd.setSelectedTitle(colored);
        p.sendMessage(ColorUtil.colorize("&a새 칭호 등록: &f") + colored);
    }
}
