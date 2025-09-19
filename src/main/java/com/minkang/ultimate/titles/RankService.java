
package com.minkang.ultimate.titles;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RankService {
    private final Main plugin;
    private final List<Rank> ranks = new ArrayList<>();

    public RankService(Main plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        ranks.clear();
        for (Map<?,?> m : plugin.getConfig().getMapList("ranks")) {
            Rank r = Rank.fromMap(m);
            if (r != null) ranks.add(r);
        }
        ranks.sort((a,b) -> Integer.compare(a.requiredMinutes, b.requiredMinutes));
    }

    public Rank getRankByMinutes(int minutes) {
        Rank current = null;
        for (Rank r : ranks) {
            if (minutes >= r.requiredMinutes) current = r;
        }
        return current;
    }

    public Rank getNextRank(int minutes) {
        for (Rank r : ranks) if (minutes < r.requiredMinutes) return r;
        return null;
    }

    public int getPlayMinutes(Player p) {
        int ticks = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
        return (int) TimeUnit.MINUTES.convert(ticks, TimeUnit.TICKS);
    }

    public void checkAndApplyRank(Player p) {
        int minutes = getPlayMinutes(p);
        Rank now = getRankByMinutes(minutes);
        if (now == null) return;
        PlayerData data = plugin.storage().get(p.getUniqueId());
        if (data.getRankId() == null || !data.getRankId().equals(now.id)) {
            data.setRankId(now.id);
            plugin.storage().save(p.getUniqueId(), data);
            plugin.luck().applyRank(p, now);
        }
    }
}
