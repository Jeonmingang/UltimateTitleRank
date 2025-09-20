package com.minkang.ultimate.titles.listeners;

import com.minkang.ultimate.titles.UltimateTitleRank;
import com.minkang.ultimate.titles.storage.PlayerData;
import com.minkang.ultimate.titles.util.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final UltimateTitleRank plugin;
    public ChatListener(UltimateTitleRank plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        String mode = plugin.getConfig().getString("general.chat_mode", "augment");
        if ("off".equalsIgnoreCase(mode)) return;

        PlayerData pd = plugin.getStorage().get(e.getPlayer().getUniqueId());
        String title = pd.getSelectedTitle();
        String colorKey = pd.getSelectedColor();
        String colorCode = resolveColorCode(colorKey);

        // colorize user's raw message with selected color (using & codes)
        String coloredMsg = ColorUtil.colorize(e.getMessage());
        if (colorCode != null && colorCode.length() > 0) {
            coloredMsg = ColorUtil.colorize(colorCode) + ChatColor.stripColor(coloredMsg);
        }

        if ("replace".equalsIgnoreCase(mode)) {
            // Old behavior: replace whole format
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
            e.setMessage(coloredMsg);
            e.setFormat(format);
            return;
        }

        // AUGMENT: keep other plugins' format; only inject title and colored message
        String fmt = e.getFormat(); // whatever other plugin decided
        // Inject title around %1$s (player name)
        if (title != null && !title.isEmpty()) {
            if ("prefix".equalsIgnoreCase(plugin.getConfig().getString("general.title_position", "suffix"))) {
                fmt = fmt.replace("%1$s", title + ChatColor.RESET + " %1$s");
            } else {
                fmt = fmt.replace("%1$s", "%1$s " + title + ChatColor.RESET);
            }
        }
        // Replace message token %2$s with our colored content
        fmt = fmt.replace("%2$s", coloredMsg + ChatColor.RESET);
        e.setFormat(fmt);
        e.setMessage(ChatColor.stripColor(coloredMsg)); // ensure no double-coloring by external plugins on message
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
