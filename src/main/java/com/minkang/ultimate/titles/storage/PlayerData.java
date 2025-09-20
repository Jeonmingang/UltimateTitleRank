package com.minkang.ultimate.titles.storage;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    private int minutesPlayed;
    private String currentRankId;
    private String selectedTitle;
    private java.util.List<String> ownedTitles;
    private java.util.List<String> colorPermissions;
    private String selectedColor;
    private int legendaryUsedToday;
    private int megabossUsedToday;

    public PlayerData(int minutesPlayed, String currentRankId, String selectedTitle,
                      java.util.List<String> ownedTitles, java.util.List<String> colorPermissions,
                      String selectedColor, int legendaryUsedToday, int megabossUsedToday) {
        this.minutesPlayed = minutesPlayed;
        this.currentRankId = currentRankId;
        this.selectedTitle = selectedTitle;
        this.ownedTitles = new java.util.ArrayList<String>(ownedTitles);
        this.colorPermissions = new java.util.ArrayList<String>(colorPermissions);
        this.selectedColor = selectedColor;
        this.legendaryUsedToday = legendaryUsedToday;
        this.megabossUsedToday = megabossUsedToday;
    }

    public int getMinutesPlayed() { return minutesPlayed; }
    public void addMinutes(int n) { this.minutesPlayed += n; }

    public String getCurrentRankId() { return currentRankId; }
    public void setCurrentRankId(String id) { this.currentRankId = id; }

    public String getSelectedTitle() { return selectedTitle; }
    public void setSelectedTitle(String t) { this.selectedTitle = t; }

    public java.util.List<String> getOwnedTitles() { return ownedTitles; }

    public java.util.List<String> getColorPermissions() { return colorPermissions; }

    public String getSelectedColor() { return selectedColor; }
    public void setSelectedColor(String c) { this.selectedColor = c; }

    public int getLegendaryUsedToday() { return legendaryUsedToday; }
    public void setLegendaryUsedToday(int n) { this.legendaryUsedToday = n; }
    public void incLegendary() { this.legendaryUsedToday++; }

    public int getMegabossUsedToday() { return megabossUsedToday; }
    public void setMegabossUsedToday(int n) { this.megabossUsedToday = n; }
    public void incMegaboss() { this.megabossUsedToday++; }
}
