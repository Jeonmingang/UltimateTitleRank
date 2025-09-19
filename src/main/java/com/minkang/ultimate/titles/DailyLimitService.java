package com.minkang.ultimate.titles;

import org.bukkit.entity.Player;

public class DailyLimitService {
    private final Main plugin;
    public DailyLimitService(Main p){ this.plugin=p; }

    public boolean tryConsumeLegendary(Player p, int max) {
        PlayerData d = plugin.storage().get(p.getUniqueId());
        if (d.getDailyLegendaryUsed() >= max) return false;
        d.setDailyLegendaryUsed(d.getDailyLegendaryUsed()+1);
        plugin.storage().save(p.getUniqueId(), d);
        return true;
    }

    public boolean tryConsumeMegaboss(Player p, int max) {
        PlayerData d = plugin.storage().get(p.getUniqueId());
        if (d.getDailyMegabossUsed() >= max) return false;
        d.setDailyMegabossUsed(d.getDailyMegabossUsed()+1);
        plugin.storage().save(p.getUniqueId(), d);
        return true;
    }
}
