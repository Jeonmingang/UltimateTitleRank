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
        if (!plugin.getConfig().getBoolean("general.chat_format_enabled", true)) return;
        PlayerData pd = plugin.getStorage().get(e.getPlayer().getUniqueId());
        String colorKey = pd.getSelectedColor();
        String colorCode = "&f";
        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("colorchat.colors");
        if (sec != null && sec.getKeys(false).contains(colorKey)) {
            colorCode = sec.getConfigurationSection(colorKey).getString("code", "&f");
        }
        String coloredMsg = com.minkang.ultimate.titles.util.ColorUtil.colorize(colorCode + org.bukkit.ChatColor.stripColor(e.getMessage()));
        String fmt = e.getFormat();
        if (fmt.toLowerCase().contains("{message}")) {
            e.setFormat(fmt.replace("{MESSAGE}", coloredMsg).replace("{message}", coloredMsg));
        } else if (fmt.contains("%2$s")) {
            e.setFormat(fmt.replace("%2$s", coloredMsg));
        } else {
            e.setFormat("%1$s: " + coloredMsg);
        }
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
        boolean done = false;
        boolean suffixMode = !"prefix".equalsIgnoreCase(plugin.getConfig().getString("general.title_position", "suffix"));

        if (suffixMode) {
            if (fmt.contains("%2$s")) {
                String insert = (!title.isEmpty() ? " " + title + ChatColor.RESET + " " : " ");
                fmt = fmt.replace("%2$s", (title.isEmpty() ? "" : insert) + "%2$s");
                fmt = fmt.replace("%2$s", coloredMsg);
                done = true;
            } else if (fmt.toLowerCase().contains("{message}")) {
                String insert = (!title.isEmpty() ? " " + title + ChatColor.RESET + " " : " ");
                fmt = fmt.replace("{MESSAGE}", (title.isEmpty() ? "" : insert) + "{MESSAGE}")
                         .replace("{message}", (title.isEmpty() ? "" : insert) + "{message}");
                fmt = fmt.replace("{MESSAGE}", coloredMsg).replace("{message}", coloredMsg);
                done = true;
            }
        } else {
            if (!title.isEmpty()) {
                if (fmt.contains("%1$s")) {
                    fmt = fmt.replace("%1$s", title + ChatColor.RESET + " %1$s");
                    if (fmt.contains("%2$s")) {
                        fmt = fmt.replace("%2$s", coloredMsg);
                    } else if (fmt.toLowerCase().contains("{message}")) {
                        fmt = fmt.replace("{MESSAGE}", coloredMsg).replace("{message}", coloredMsg);
                    }
                    done = true;
                } else if (fmt.contains("{DISPLAYNAME}")) {
                    fmt = fmt.replace("{DISPLAYNAME}", title + ChatColor.RESET + " {DISPLAYNAME}");
                    if (fmt.toLowerCase().contains("{message}")) {
                        fmt = fmt.replace("{MESSAGE}", coloredMsg).replace("{message}", coloredMsg);
                    }
                    done = true;
                }
            } else {
                if (fmt.contains("%2$s")) {
                    fmt = fmt.replace("%2$s", coloredMsg);
                    done = true;
                } else if (fmt.toLowerCase().contains("{message}")) {
                    fmt = fmt.replace("{MESSAGE}", coloredMsg).replace("{message}", coloredMsg);
                    done = true;
                }
            }
        }

        if (!done) {
            String playerName = e.getPlayer().getDisplayName();
            String mid = title.isEmpty() ? " " : " " + title + ChatColor.RESET + " ";
            fmt = playerName + mid + ": " + coloredMsg;
        }

        boolean force = plugin.getConfig().getBoolean("general.force_replace_when_missing", false);
        if (force && !title.isEmpty()) {
            String plainFmt = ChatColor.stripColor(fmt);
            String plainTitle = ChatColor.stripColor(title);
            if (!plainFmt.contains(plainTitle)) {
                String playerName = e.getPlayer().getDisplayName();
                fmt = playerName + " " + title + ChatColor.RESET + ": " + coloredMsg;
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
