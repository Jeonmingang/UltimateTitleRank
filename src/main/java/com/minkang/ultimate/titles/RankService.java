package com.minkang.ultimate.titles;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RankService {
    private final Main plugin;
    private final List<Rank> ranks = new ArrayList<>();

    public RankService(Main plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        ranks.clear();
        List<Map<?,?>> list = (List<Map<?,?>>) plugin.getConfig().getList("ranks");
        if (list != null) {
            for (Map<?,?> m : list) {
                try { ranks.add(Rank.fromMap(m)); } catch (Exception ignored) {}
            }
            Collections.sort(ranks, Comparator.comparingInt(r -> r.requiredMinutes));
        }
    }

    public int getPlayMinutes(Player p) {
        // MC 1.16.5: PLAY_ONE_MINUTE is in ticks
        int ticks = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
        return ticks / 20 / 60;
    }

    public Rank getRankByMinutes(int minutes) {
        Rank current = null;
        for (Rank r : ranks) {
            if (minutes >= r.requiredMinutes) current = r;
            else break;
        }
        return current;
    }

    public Rank getNextRank(int minutes) {
        for (Rank r : ranks) {
            if (minutes < r.requiredMinutes) return r;
        }
        return null;
    }

    public void checkAndApplyRank(Player p) {
        int minutes = getPlayMinutes(p);
        Rank now = getRankByMinutes(minutes);
        if (now == null) return;
        PlayerData data = plugin.storage().get(p.getUniqueId());
        if (data.getRankId() == null || !data.getRankId().equals(now.id)) {
            String prevGroup = data.getLastRankGroup();
            data.setRankId(now.id);
            data.setLastRankGroup(now.group);
            plugin.storage().save(p.getUniqueId(), data);
            plugin.luck().applyRank(plugin, p, now); // use console command template
            p.sendMessage(Chat.color(plugin.msg("prefix") + "&a자동 승급됨: &f" + now.display));
            Rank next = getNextRank(minutes);
            if (next != null) {
                int need = next.requiredMinutes - minutes;
                p.sendMessage(Chat.color("&7다음 등급까지 &f" + need + "분 &7남음 → " + next.display + (next.group != null ? " &8(" + next.group + ")" : "")));
            }
        }
    }
}
