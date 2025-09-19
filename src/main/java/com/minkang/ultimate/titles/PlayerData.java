package com.minkang.ultimate.titles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerData {
    // Color chat
    private Set<String> colorsOwned = new HashSet<>();
    private String activeColorId = null;

    // Rank & titles
    private String rankId;
    private Map<Integer, String> fixedTitles = new HashMap<>();
    private Map<Integer, String> customTitles = new HashMap<>();

    // Daily limits
    private int dailyLegendaryUsed = 0;
    private int dailyMegabossUsed = 0;
    private String dailyDate = "";

    // --- Rank ---
    public String getRankId() { return rankId; }
    public void setRankId(String rankId) { this.rankId = rankId; }

    // --- Titles ---
    public Map<Integer, String> getFixedTitles() { return fixedTitles; }
    public Map<Integer, String> getCustomTitles() { return customTitles; }

    // --- Daily limits ---
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

    // --- Color chat ---
    public Set<String> getColorsOwned() { return colorsOwned; }
    public String getActiveColorId() { return activeColorId; }
    public void setActiveColorId(String id) { this.activeColorId = id; }
}
