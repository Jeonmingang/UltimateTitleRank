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
    public ChatListener(UltimateTitleRank plugin) {
        this.plugin = plugin;
    }

    // Only colorize the message body so other plugins' prefixes/suffixes stay intact.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        PlayerData pd = plugin.getStorage().get(e.getPlayer().getUniqueId());
        String colorKey = pd.getSelectedColor();
        String code = "&f";
        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("colorchat.colors");
        if (sec != null && colorKey != null && sec.getKeys(false).contains(colorKey)) {
            code = sec.getConfigurationSection(colorKey).getString("code", "&f");
        }
        String raw = e.getMessage();
        String colored = ColorUtil.colorize(code + ChatColor.stripColor(raw == null ? "" : raw));
        e.setMessage(colored);
    }
}
