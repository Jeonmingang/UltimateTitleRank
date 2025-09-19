
package com.minkang.ultimate.titles;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public class Rank {
    public final String id;
    public final String display;
    public final int requiredMinutes;
    public final String group;
    public final String suffix;
    public final int limitLegendary;
    public final int limitMegaboss;

    public Rank(String id, String display, int requiredMinutes, String group, String suffix, int l, int m) {
        this.id = id;
        this.display = display;
        this.requiredMinutes = requiredMinutes;
        this.group = group;
        this.suffix = suffix;
        this.limitLegendary = l;
        this.limitMegaboss = m;
    }

    public static Rank from(ConfigurationSection cs) {
        if (cs == null) return null;
        String id = cs.getString("id");
        String display = cs.getString("display");
        int req = cs.getInt("required_minutes");
        String group = cs.getString("luckperms_group", null);
        String suffix = cs.getString("suffix", "");
        ConfigurationSection vl = cs.getConfigurationSection("view_limits");
        int l = vl == null ? 0 : vl.getInt("legendary_per_day", 0);
        int m = vl == null ? 0 : vl.getInt("megaboss_per_day", 0);
        return new Rank(id, display, req, group, suffix, l, m);
    }

    @SuppressWarnings("unchecked")
    public static Rank fromMap(Map<?,?> map) {
        String id = String.valueOf(map.get("id"));
        String display = String.valueOf(map.get("display"));
        int req = ((Number)map.get("required_minutes")).intValue();
        String group = map.containsKey("luckperms_group") ? String.valueOf(map.get("luckperms_group")) : null;
        String suffix = map.containsKey("suffix") ? String.valueOf(map.get("suffix")) : "";
        Map<String,Object> vl = map.containsKey("view_limits") ? (Map<String,Object>) map.get("view_limits") : null;
        int l = vl != null && vl.get("legendary_per_day") != null ? ((Number)vl.get("legendary_per_day")).intValue() : 0;
        int m = vl != null && vl.get("megaboss_per_day") != null ? ((Number)vl.get("megaboss_per_day")).intValue() : 0;
        return new Rank(id, display, req, group, suffix, l, m);
    }
}
