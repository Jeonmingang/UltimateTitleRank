package com.minkang.ultimate.titles;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class LuckPermsHook {
    public void applyRank(Main plugin, Player p, Rank rank) {
        if (rank == null || rank.group == null || rank.group.isEmpty()) return;
        if (!plugin.getConfig().getBoolean("auto_promote.enabled", true)) return;
        String addTmpl = plugin.getConfig().getString("auto_promote.add_command", "lp user {player} parent add {group}");
        String remTmpl = plugin.getConfig().getString("auto_promote.remove.remove_command", "lp user {player} parent remove {group}");
        boolean remEnabled = plugin.getConfig().getBoolean("auto_promote.remove.enabled", false);
        boolean remPrev = plugin.getConfig().getBoolean("auto_promote.remove.remove_previous_rank_group", true);
        List<String> remExtra = plugin.getConfig().getStringList("auto_promote.remove.groups_extra");

        String addCmd = addTmpl.replace("{player}", p.getName()).replace("{uuid}", p.getUniqueId().toString()).replace("{group}", rank.group);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), addCmd);

        if (remEnabled) {
            PlayerData d = plugin.storage().get(p.getUniqueId());
            String prev = d.getLastRankGroup();
            if (remPrev && prev != null && !prev.equals(rank.group)) {
                String cmd = remTmpl.replace("{player}", p.getName()).replace("{uuid}", p.getUniqueId().toString()).replace("{group}", prev);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
            if (remExtra != null) {
                for (String g : remExtra) {
                    String cmd = remTmpl.replace("{player}", p.getName()).replace("{uuid}", p.getUniqueId().toString()).replace("{group}", g);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        }
    }
}
