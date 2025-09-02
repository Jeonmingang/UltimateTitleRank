
package com.minkang.ultimate.listeners;

import com.minkang.ultimate.UltimateTitleRank;
import com.minkang.ultimate.util.Texts;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final UltimateTitleRank plugin;

    public ChatListener(UltimateTitleRank plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        // Awaiting custom title input via voucher
        com.minkang.ultimate.managers.TitleManager tm = plugin.getTitleManager();
        Player playerAwait = event.getPlayer();
        if (tm.isAwaiting(playerAwait.getUniqueId())) {
            String msgRaw = event.getMessage();
            String cancelKw = plugin.getConfig().getString("custom_title_voucher.cancel_keyword", "취소");
            if (msgRaw.equalsIgnoreCase(cancelKw)) {
                tm.endAwaiting(playerAwait.getUniqueId());
                String cmsg = plugin.getConfig().getString("custom_title_voucher.cancel_message", "&7입력을 취소했습니다.");
                playerAwait.sendMessage(Texts.color(cmsg));
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
                playerAwait.sendMessage(Texts.color(ok));
            }
            tm.endAwaiting(playerAwait.getUniqueId());
            event.setCancelled(true);
            return;
        }

        Player p = event.getPlayer();
        String selected = plugin.getTitleManager().getSelectedTitle(p.getUniqueId());
        if (selected == null || selected.trim().isEmpty()) {
            return; // keep existing chat format from other plugins
        }

        String prefixTemplate = plugin.getConfig().getString("chat.prefix_template", "&7[ &f%title% &7] ");
        String coloredPrefix = Texts.color(prefixTemplate.replace("%title%", selected));
        String baseFormat = event.getFormat(); // preserve other plugins

        boolean enableOpColor = plugin.getConfig().getBoolean("chat.enable_op_color", true);
        String opColor = plugin.getConfig().getString("chat.op_name_color", "&c");
        String injectMode = plugin.getConfig().getString("chat.inject_mode", "prepend").toLowerCase();

        if ("replace_name".equals(injectMode)) {
            String nameToken = "%1$s";
            String nameReplacement;
            if (p.isOp() && enableOpColor) {
                nameReplacement = coloredPrefix + Texts.color(opColor) + nameToken + ChatColor.RESET;
            } else {
                nameReplacement = coloredPrefix + nameToken;
            }
            if (baseFormat != null && baseFormat.contains(nameToken)) {
                event.setFormat(baseFormat.replace(nameToken, nameReplacement));
            } else {
                event.setFormat(nameReplacement + ChatColor.WHITE + " " + "%2$s");
            }
        } else { // prepend
            String newFormat = coloredPrefix + baseFormat;
            if (enableOpColor && p.isOp() && baseFormat != null && baseFormat.contains("%1$s")) {
                newFormat = newFormat.replace("%1$s", Texts.color(opColor) + "%1$s" + ChatColor.RESET);
            }
            event.setFormat(newFormat);
        }
    }
}
