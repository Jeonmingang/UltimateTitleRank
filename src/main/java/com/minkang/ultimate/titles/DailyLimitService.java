
package com.minkang.ultimate.titles;

import org.bukkit.entity.Player;

public class DailyLimitService {
    private final Main plugin;
    public DailyLimitService(Main p){ this.plugin=p; }

    public boolean consumeLegendary(Player p) {
        PlayerData d = plugin.storage().get(p.getUniqueId());
        Rank r = plugin.rankService().getRankByMinutes(plugin.rankService().getPlayMinutes(p));
        if (r == null) return false;
        if (d.getDailyLegendaryUsed() >= r.limitLegendary) return false;
        d.setDailyLegendaryUsed(d.getDailyLegendaryUsed()+1);
        plugin.storage().save(p.getUniqueId(), d);
        return true;
    }

    public boolean consumeMegaboss(Player p) {
        PlayerData d = plugin.storage().get(p.getUniqueId());
        Rank r = plugin.rankService().getRankByMinutes(plugin.rankService().getPlayMinutes(p));
        if (r == null) return false;
        if (d.getDailyMegabossUsed() >= r.limitMegaboss) return false;
        d.setDailyMegabossUsed(d.getDailyMegabossUsed()+1);
        plugin.storage().save(p.getUniqueId(), d);
        return true;
    }
}
