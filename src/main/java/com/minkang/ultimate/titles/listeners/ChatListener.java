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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        String mode = plugin.getConfig().getString("general.chat_mode", "augment");
        if ("off".equalsIgnoreCase(mode)) return;

        PlayerData pd = plugin.getStorage().get(e.getPlayer().getUniqueId());
        String title = pd.getSelectedTitle() == null ? "" : pd.getSelectedTitle();
        String colorKey = pd.getSelectedColor();
        String colorCode = resolveColorCode(colorKey);

        String coloredMsg = ColorUtil.colorize(e.getMessage());
        if (colorCode != null && colorCode.length() > 0) {
            coloredMsg = ColorUtil.colorize(colorCode) + ChatColor.stripColor(coloredMsg) + ChatColor.RESET;
        }

        if ("replace".equalsIgnoreCase(mode)) {
            String playerName = e.getPlayer().getDisplayName();
            String format;
            if (!title.isEmpty()) {
                if ("prefix".equalsIgnoreCase(plugin.getConfig().getString("general.title_position", "suffix"))) {
                    format = title + ChatColor.RESET + " " + playerName + ChatColor.RESET + ": " + coloredMsg;
                } else {
                    format = playerName + " " + title + ChatColor.RESET + ": " + coloredMsg;
                }
            } else {
                format = playerName + ChatColor.RESET + ": " + coloredMsg;
            }
            e.setFormat(format);
            return;
        }

        String fmt = e.getFormat();
        boolean injected = false;

        if (fmt.contains("%1$s")) {
            if (!title.isEmpty()) {
                if ("prefix".equalsIgnoreCase(plugin.getConfig().getString("general.title_position", "suffix"))) {
                    fmt = fmt.replace("%1$s", title + ChatColor.RESET + " %1$s");
                } else {
                    fmt = fmt.replace("%1$s", "%1$s " + title + ChatColor.RESET);
                }
            }
            if (fmt.contains("%2$s")) {
                fmt = fmt.replace("%2$s", coloredMsg);
                injected = true;
            }
        } else if (fmt.toLowerCase().contains("{message}")) {
            fmt = fmt.replace("{MESSAGE}", coloredMsg).replace("{message}", coloredMsg);
            if (!title.isEmpty()) {
                if (fmt.contains("{DISPLAYNAME}")) {
                    if ("prefix".equalsIgnoreCase(plugin.getConfig().getString("general.title_position", "suffix"))) {
                        fmt = fmt.replace("{DISPLAYNAME}", title + ChatColor.RESET + " {DISPLAYNAME}");
                    } else {
                        fmt = fmt.replace("{DISPLAYNAME}", "{DISPLAYNAME} " + title + ChatColor.RESET);
                    }
                }
            }
            injected = true;
        }

        if (!injected) {
            String playerName = e.getPlayer().getDisplayName();
            String prefixPart = "";
            String suffixPart = "";
            if (!title.isEmpty()) {
                if ("prefix".equalsIgnoreCase(plugin.getConfig().getString("general.title_position", "suffix"))) {
                    prefixPart = title + ChatColor.RESET + " ";
                } else {
                    suffixPart = " " + title + ChatColor.RESET;
                }
            }
            fmt = prefixPart + playerName + suffixPart + ChatColor.RESET + ": " + coloredMsg;
        }

        boolean force = plugin.getConfig().getBoolean("general.force_replace_when_missing", true);
        if (force && !title.isEmpty()) {
            String plainFmt = ChatColor.stripColor(fmt);
            String plainTitle = ChatColor.stripColor(title);
            if (!plainFmt.contains(plainTitle)) {
                String playerName = e.getPlayer().getDisplayName();
                if ("prefix".equalsIgnoreCase(plugin.getConfig().getString("general.title_position", "suffix"))) {
                    fmt = title + ChatColor.RESET + " " + playerName + ChatColor.RESET + ": " + coloredMsg;
                } else {
                    fmt = playerName + " " + title + ChatColor.RESET + ": " + coloredMsg;
                }
            }
        }
        e.setFormat(fmt);
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
