
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
