
package com.minkang.ultimate.listeners;

import com.minkang.ultimate.UltimateTitleRank;
import com.minkang.ultimate.util.Texts;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class ChatListener implements Listener {

    private final UltimateTitleRank plugin;

    public ChatListener(UltimateTitleRank plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        // Awaiting custom title input via voucher
        com.minkang.ultimate.managers.TitleManager tm = plugin.getTitleManager();
        org.bukkit.entity.Player playerAwait = event.getPlayer();
        if (tm.isAwaiting(playerAwait.getUniqueId())) {
            String msgRaw = event.getMessage();
            String cancelKw = plugin.getConfig().getString("custom_title_voucher.cancel_keyword", "취소");
            if (msgRaw.equalsIgnoreCase(cancelKw)) {
                tm.endAwaiting(playerAwait.getUniqueId());
                String cmsg = plugin.getConfig().getString("custom_title_voucher.cancel_message", "&7입력을 취소했습니다.");
                playerAwait.sendMessage(com.minkang.ultimate.util.Texts.color(cmsg));
                event.setCancelled(true);
                return;
            }
            String title = msgRaw.trim();
            if (!title.isEmpty()) {
                tm.addGlobalTitle(title);
                tm.addOwnedTitle(playerAwait.getUniqueId(), title);
                tm.setSelectedTitle(playerAwait.getUniqueId(), title);
                String ok = plugin.getConfig().getString("custom_title_voucher.success_message", "&a커스텀 칭호 등록 완료: &f%title%");
                ok = ok.replace("%title%", title);
                playerAwait.sendMessage(com.minkang.ultimate.util.Texts.color(ok));
            }
            tm.endAwaiting(playerAwait.getUniqueId());
            event.setCancelled(true);
            return;
        }
        Player p = event.getPlayer();
        String selected = plugin.getTitleManager().getSelectedTitle(p.getUniqueId());
        String prefixTemplate = plugin.getConfig().getString("chat.prefix_template", "&7[&f%title%&7] ");
        String prefix = selected == null ? "" : Texts.color(prefixTemplate.replace("%title%", selected));

        String name = p.getName();
        if (p.isOp()) {
            String color = plugin.getConfig().getString("chat.op_name_color", "&c");
            name = Texts.color(color) + name + ChatColor.RESET;
        }

        String format = prefix + name + ChatColor.WHITE + ": " + ChatColor.RESET + "%2$s";
        // Keep recipients & message; we only adjust format
        event.setFormat(format);
    }
}
