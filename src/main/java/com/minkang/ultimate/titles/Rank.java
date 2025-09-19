package com.minkang.ultimate.titles;

import java.util.Map;

public class Rank {
    public final String id;
    public final String display;
    public final int requiredMinutes;
    public final String group;
    public final int limitLegendary;
    public final int limitMegaboss;

    public Rank(String id, String display, int requiredMinutes, String group, int limitLegendary, int limitMegaboss) {
        this.id = id;
        this.display = display;
        this.requiredMinutes = requiredMinutes;
        this.group = group;
        this.limitLegendary = limitLegendary;
        this.limitMegaboss = limitMegaboss;
    }

    @SuppressWarnings("unchecked")
    public static Rank fromMap(Map<?,?> map) {
        String id = String.valueOf(map.get("id"));
        String display = String.valueOf(map.get("display"));
        int req = ((Number)map.get("required_minutes")).intValue();
        String group = map.containsKey("luckperms_group") ? String.valueOf(map.get("luckperms_group")) : null;
        Map<String,Object> vl = map.containsKey("view_limits") ? (Map<String,Object>) map.get("view_limits") : null;
        int l = vl != null && vl.get("legendary_per_day") != null ? ((Number)vl.get("legendary_per_day")).intValue() : 0;
        int m = vl != null && vl.get("megaboss_per_day") != null ? ((Number)vl.get("megaboss_per_day")).intValue() : 0;
        return new Rank(id, display, req, group, l, m);
    }
}
