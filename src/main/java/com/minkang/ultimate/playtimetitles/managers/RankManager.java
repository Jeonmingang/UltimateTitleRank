package com.minkang.ultimate.playtimetitles.managers;

import com.minkang.ultimate.playtimetitles.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RankManager {
    public static class Rank {
        public String id;
        public String display;
        public int requiredMinutes;
        public String group;
        public String suffix;
        public int legendaryDaily;
        public int megabossDaily;
    }

    private final Main plugin;
    private final LuckPermsBridge lp;
    private final List<Rank> ranks = new ArrayList<>();

    public RankManager(Main plugin, LuckPermsBridge lp) {
        this.plugin = plugin;
        this.lp = lp;
        reload();
    }

    public void reload() {
        ranks.clear();
        FileConfiguration c = plugin.getConfig();
        List<?> list = c.getList("ranks");
        if (list == null) return;
        for (Object o : list) {
            if (!(o instanceof ConfigurationSection)) continue;
        }
        for (int i=0; i<list.size(); i++) {
            ConfigurationSection s = c.getConfigurationSection("ranks." + i);
            if (s == null) continue;
            Rank r = new Rank();
            r.id = s.getString("id","r"+i);
            r.display = colorize(s.getString("display","&7R"+i));
            r.requiredMinutes = s.getInt("required_minutes", 0);
            r.group = s.getString("group", "default");
            r.suffix = colorize(s.getString("suffix","&7[★]"));
            r.legendaryDaily = s.getInt("legendary_daily", 1);
            r.megabossDaily = s.getInt("megaboss_daily", 1);
            ranks.add(r);
        }
        ranks.sort((a,b)->Integer.compare(a.requiredMinutes, b.requiredMinutes));
    }

    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public Rank getCurrentRank(UUID u) {
        int mins = plugin.getPlaytimeManager().getMinutes(u);
        Rank cur = null;
        for (Rank r : ranks) {
            if (mins >= r.requiredMinutes) cur = r;
            else break;
        }
        return cur;
    }

    public Rank getNextRank(UUID u) {
        int mins = plugin.getPlaytimeManager().getMinutes(u);
        for (Rank r : ranks) {
            if (mins < r.requiredMinutes) return r;
        }
        return null;
    }

    public void tryPromote(Player p) {
        Rank current = getCurrentRank(p.getUniqueId());
        if (current == null) return;
        // Ensure LP group matches current rank
        if (lp.isAvailable()) {
            // remove all configured groups first, then add current
            for (Rank r : ranks) {
                if (!r.group.equalsIgnoreCase(current.group)) {
                    lp.removeGroup(p, r.group);
                }
            }
            lp.addGroup(p, current.group);
            lp.setSuffix(p, current.suffix);
        } else {
            // Fallback: just send info
            p.sendMessage("§a현재 등급: " + current.display + " §7(LP 미탑재로 그룹 적용 생략)");
        }
    }

    public List<Rank> getRanks() { return ranks; }
}
