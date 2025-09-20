package com.minkang.ultimate.titles.listeners;

import com.minkang.ultimate.titles.UltimateTitleRank;
import com.minkang.ultimate.titles.storage.PlayerData;
import com.minkang.ultimate.titles.util.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final UltimateTitleRank plugin;
    public ChatListener(UltimateTitleRank plugin) { this.plugin = plugin; }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (!plugin.getConfig().getBoolean("general.chat_format_enabled", true)) return;
        PlayerData pd = plugin.getStorage().get(e.getPlayer().getUniqueId());
        String title = pd.getSelectedTitle();
        String colorKey = pd.getSelectedColor();
        String colorCode = resolveColorCode(colorKey);
        String msg = e.getMessage();

        String coloredMsg = ColorUtil.colorize(msg);
        if (colorCode != null && colorCode.length() > 0) {
            coloredMsg = ColorUtil.colorize(colorCode) + ChatColor.stripColor(coloredMsg);
        }

        String playerName = e.getPlayer().getDisplayName();
        String format;
        if (title != null && !title.isEmpty()) {
            if ("prefix".equalsIgnoreCase(plugin.getConfig().getString("general.title_position", "suffix"))) {
                format = title + ChatColor.RESET + " " + playerName + ChatColor.RESET + ": " + coloredMsg;
            } else {
                format = playerName + " " + title + ChatColor.RESET + ": " + coloredMsg;
            }
        } else {
            format = playerName + ChatColor.RESET + ": " + coloredMsg;
        }
        e.setFormat(format);
    }

    private String resolveColorCode(String key) {
        if (key == null) return "&f";
        org.bukkit.configuration.ConfigurationSection sec = UltimateTitleRank.getInstance().getConfig().getConfigurationSection("colorchat.colors");
        if (sec == null) return "&f";
        if (sec.getKeys(false).contains(key)) {
            return sec.getConfigurationSection(key).getString("code", "&f");
        }
        return "&f";
    }
}
