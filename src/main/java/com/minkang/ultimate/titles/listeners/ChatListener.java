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

    // Keep other plugins' prefixes/suffixes/format intact; only colorize the message body.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        PlayerData pd = plugin.getStorage().get(e.getPlayer().getUniqueId());
        String key = pd.getSelectedColor();
        String code = "&f";
        org.bukkit.configuration.ConfigurationSection sec =
                plugin.getConfig().getConfigurationSection("colorchat.colors");
        if (sec != null && key != null && sec.getKeys(false).contains(key)) {
            code = sec.getConfigurationSection(key).getString("code", "&f");
        }
        String raw = e.getMessage();
        String colored = ColorUtil.colorize(code + (raw == null ? "" : ChatColor.stripColor(raw)));
        e.setMessage(colored);
    }
}
