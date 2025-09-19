
package com.minkang.ultimate.titles;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CommandInterceptListener implements Listener {
    private final Main plugin;
    private final DailyLimitService limits;
    private final Set<UUID> bypassOnce = new HashSet<>();

    public CommandInterceptListener(Main p, DailyLimitService s){ this.plugin=p; this.limits=s; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCmd(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (bypassOnce.remove(p.getUniqueId())) return; // allow original run once

        String msg = e.getMessage().toLowerCase();
        for (String alias : plugin.getConfig().getStringList("intercept_commands.legendary_aliases")) {
            if (msg.startsWith(alias)) {
                e.setCancelled(true);
                if (!limits.consumeLegendary(p)) {
                    p.sendMessage(Chat.color(plugin.msg("prefix")+plugin.msg("limits.no_more_legendary")));
                    return;
                }
                String dispatch = plugin.getConfig().getString("intercept_commands.dispatch_on_legendary", "");
                if (dispatch != null && !dispatch.isEmpty()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), dispatch.replace("%player%", p.getName()));
                } else {
                    bypassOnce.add(p.getUniqueId());
                    Bukkit.dispatchCommand(p, msg.substring(1));
                }
                return;
            }
        }
        for (String alias : plugin.getConfig().getStringList("intercept_commands.megaboss_aliases")) {
            if (msg.startsWith(alias)) {
                e.setCancelled(true);
                if (!limits.consumeMegaboss(p)) {
                    p.sendMessage(Chat.color(plugin.msg("prefix")+plugin.msg("limits.no_more_megaboss")));
                    return;
                }
                String dispatch = plugin.getConfig().getString("intercept_commands.dispatch_on_megaboss", "");
                if (dispatch != null && !dispatch.isEmpty()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), dispatch.replace("%player%", p.getName()));
                } else {
                    bypassOnce.add(p.getUniqueId());
                    Bukkit.dispatchCommand(p, msg.substring(1));
                }
                return;
            }
        }
    }
}
