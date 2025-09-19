
package com.minkang.ultimate.titles;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {
    private java.util.Set<String> colorsOwned = new java.util.HashSet<>();
    private String activeColorId = null;
    private String rankId;
    private Map<Integer, String> fixedTitles = new HashMap<>();
    private Map<Integer, String> customTitles = new HashMap<>();
    private int dailyLegendaryUsed = 0;
    private int dailyMegabossUsed = 0;
    private String dailyDate = "";

    public String getRankId() { return rankId; }
    public void setRankId(String id) { this.rankId = id; }

    public Map<Integer, String> getFixedTitles() { return fixedTitles; }
    public Map<Integer, String> getCustomTitles() { return customTitles; }

    public int getDailyLegendaryUsed() { return dailyLegendaryUsed; }
    public void setDailyLegendaryUsed(int v) { this.dailyLegendaryUsed = v; }
    public int getDailyMegabossUsed() { return dailyMegabossUsed; }
    public void setDailyMegabossUsed(int v) { this.dailyMegabossUsed = v; }

    public void ensureToday(String today) {
        if (!today.equals(dailyDate)) {
            dailyDate = today;
            dailyLegendaryUsed = 0;
            dailyMegabossUsed = 0;
        }
    }
}


    public java.util.Set<String> getColorsOwned() { return colorsOwned; }
    public String getActiveColorId() { return activeColorId; }
    public void setActiveColorId(String id) { this.activeColorId = id; }
