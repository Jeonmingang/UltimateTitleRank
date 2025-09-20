package com.minkang.ultimate.titles.listeners;

import com.minkang.ultimate.titles.UltimateTitleRank;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ForceCheckspawnsListener implements Listener {
    private final UltimateTitleRank plugin;
    public ForceCheckspawnsListener(UltimateTitleRank plugin){ this.plugin = plugin; }

    // HIGHEST: "world"에서 /checkspawns를 pixelmon 네임스페이스로 강제
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPreprocessHigh(PlayerCommandPreprocessEvent e){
        String msg = e.getMessage().trim();
        if (!msg.toLowerCase().startsWith("/checkspawns")) return;
        String world = e.getPlayer().getWorld().getName();
        if ("world".equalsIgnoreCase(world)) {
            String rest = msg.length() > "/checkspawns".length() ? msg.substring("/checkspawns".length()).trim() : "";
            e.setMessage("/pixelmon:checkspawns " + rest);
        }
    }

    // MONITOR: 다른 플러그인이 취소했으면 콘솔로 재실행 -> 씹힘 방지
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPreprocessMonitor(PlayerCommandPreprocessEvent e){
        String msg = e.getMessage().trim();
        String lower = msg.toLowerCase();
        if (!(lower.startsWith("/checkspawns") || lower.startsWith("/pixelmon:checkspawns"))) return;
        if (!e.isCancelled()) return;
        String rest;
        if (lower.startsWith("/checkspawns")) {
            rest = msg.substring("/checkspawns".length()).trim();
        } else {
            rest = msg.substring("/pixelmon:checkspawns".length()).trim();
        }
        e.setCancelled(true);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pixelmon:checkspawns " + rest);
    }
}
