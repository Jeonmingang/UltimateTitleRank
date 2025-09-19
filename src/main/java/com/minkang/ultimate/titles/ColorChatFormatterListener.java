package com.minkang.ultimate.titles;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ColorChatFormatterListener implements Listener {
    private final Main plugin;
    private final ColorChatService service;

    public ColorChatFormatterListener(Main plugin, ColorChatService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        PlayerData d = plugin.storage().get(e.getPlayer().getUniqueId());
        String id = d.getActiveColorId();
        if (id == null) return;
        ColorDef def = service.get(id);
        if (def == null) return;
        e.setMessage(Chat.color(def.code + e.getMessage()));
        e.setFormat("%1$s: %2$s");
    }
}
