package com.minkang.ultimate.titles;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandInterceptListener implements Listener {
    private final Main plugin;
    private final DailyLimitService limit;

    public CommandInterceptListener(Main p, DailyLimitService l){ this.plugin=p; this.limit=l; }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage().toLowerCase();
        if (msg.startsWith("/전설시간")) {
            e.setCancelled(!limit.tryConsumeLegendary(e.getPlayer(), 3));
            if (e.isCancelled()) e.getPlayer().sendMessage(Chat.color(plugin.msg("prefix") + "&c오늘의 전설시간 조회 한도를 초과했습니다."));
        } else if (msg.startsWith("/메가보스")) {
            e.setCancelled(!limit.tryConsumeMegaboss(e.getPlayer(), 3));
            if (e.isCancelled()) e.getPlayer().sendMessage(Chat.color(plugin.msg("prefix") + "&c오늘의 메가보스 조회 한도를 초과했습니다."));
        }
    }
}
