package com.minkang.ultimate.titles.listeners;

import com.minkang.ultimate.titles.UltimateTitleRank;
import com.minkang.ultimate.titles.storage.PlayerData;
import com.minkang.ultimate.titles.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlaytimeListener implements Listener {
    private final UltimateTitleRank plugin;
    public PlaytimeListener(UltimateTitleRank plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) { plugin.getStorage().get(e.getPlayer().getUniqueId()); }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) { plugin.getStorage().save(); }

    public static void tickOnlinePlayers() {
        UltimateTitleRank plugin = UltimateTitleRank.getInstance();
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData pd = plugin.getStorage().get(p.getUniqueId());
            pd.addMinutes(1);
            applyPromotionIfEligible(p, pd);
        }
        plugin.getStorage().save();
    }

    private static void applyPromotionIfEligible(Player player, PlayerData pd) {
        UltimateTitleRank plugin = UltimateTitleRank.getInstance();
        java.util.List<java.util.Map<?, ?>> ranks = plugin.getConfig().getMapList("ranks");
        String current = pd.getCurrentRankId() == null ? "" : pd.getCurrentRankId();
        String nextId = null;
        String prevGroup = null;
        String nextGroup = null;
        int minutes = pd.getMinutesPlayed();
        int bestRequired = -1;

        for (java.util.Map<?, ?> m : ranks) {
            String id = String.valueOf(m.get("id"));
            int req = ((Number)m.get("required_minutes")).intValue();
            String lp = String.valueOf(((java.util.Map)m).get("luckperms_group"));
            if (minutes >= req) {
                if (req > bestRequired) {
                    bestRequired = req;
                    nextId = id;
                    nextGroup = lp;
                }
            }
        }
        if (nextId != null && !nextId.equals(current)) {
            prevGroup = resolveGroupForId(current);
            pd.setCurrentRankId(nextId);
            if (plugin.getLpUtils().isAvailable()) {
                plugin.getLpUtils().setPrimaryGroup(player, nextGroup, prevGroup);
            }
            player.sendMessage(ColorUtil.colorize("&6[승급] &f플레이타임 " + minutes + "분 달성! 등급이 &e" + nextId + " &f로 승급되었습니다."));
        }
    }

    private static String resolveGroupForId(String id) {
        if (id == null || id.isEmpty()) return null;
        UltimateTitleRank plugin = UltimateTitleRank.getInstance();
        java.util.List<java.util.Map<?, ?>> ranks = plugin.getConfig().getMapList("ranks");
        for (java.util.Map<?, ?> m : ranks) {
            String rid = String.valueOf(m.get("id"));
            if (rid.equals(id)) {
                Object lp = m.get("luckperms_group");
                if (lp != null) return String.valueOf(lp);
            }
        }
        return null;
    }
}
