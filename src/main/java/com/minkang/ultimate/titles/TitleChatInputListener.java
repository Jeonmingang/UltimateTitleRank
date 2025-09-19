package com.minkang.ultimate.titles;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TitleChatInputListener implements Listener {
    private final Main plugin;
    private final Map<UUID, Long> pending = new ConcurrentHashMap<>();

    public TitleChatInputListener(Main plugin) { this.plugin = plugin; }

    public void await(Player p) { pending.put(p.getUniqueId(), System.currentTimeMillis()); }

    public boolean isPending(Player p) {
        Long t = pending.get(p.getUniqueId());
        if (t == null) return false;
        if (System.currentTimeMillis() - t > 30_000) { pending.remove(p.getUniqueId()); return false; }
        return true;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!isPending(p)) return;
        e.setCancelled(true);
        pending.remove(p.getUniqueId());
        String raw = e.getMessage();
        if (raw == null || raw.trim().isEmpty()) {
            p.sendMessage(Chat.color(plugin.msg("prefix") + "&c빈 칭호는 사용할 수 없습니다."));
            return;
        }
        PlayerData d = plugin.storage().get(p.getUniqueId());
        int slot = 1; while (d.getCustomTitles().containsKey(slot)) slot++;
        plugin.titles().setCustomTitle(p.getUniqueId(), slot, raw);
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> plugin.titles().applyTitleSuffix(p, raw, slot));
        p.sendMessage(Chat.color(plugin.msg("prefix") + "&a칭호가 등록/적용되었습니다: &f" + raw));
    }
}
