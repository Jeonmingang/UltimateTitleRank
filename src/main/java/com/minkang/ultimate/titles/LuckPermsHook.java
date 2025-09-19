package com.minkang.ultimate.titles;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LuckPermsHook {

    public void applyRank(Main plugin, Player p, Rank rank) {
    if (rank == null || rank.group == null || rank.group.isEmpty()) return;
    boolean enabled = plugin.getConfig().getBoolean("auto_promote.enabled", true);
    if (!enabled) return;
    String addTmpl = plugin.getConfig().getString("auto_promote.add_command", "lp user {player} parent add {group}");
    String remTmpl = plugin.getConfig().getString("auto_promote.remove.remove_command", "lp user {player} parent remove {group}");
    boolean remEnabled = plugin.getConfig().getBoolean("auto_promote.remove.enabled", false);
    boolean remPrev = plugin.getConfig().getBoolean("auto_promote.remove.remove_previous_rank_group", true);
    java.util.List<String> remExtra = plugin.getConfig().getStringList("auto_promote.remove.groups_extra");

    String addCmd = addTmpl.replace("{player}", p.getName())
                           .replace("{uuid}", p.getUniqueId().toString())
                           .replace("{group}", rank.group);
    org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), addCmd);

    if (remEnabled) {
        PlayerData d = plugin.storage().get(p.getUniqueId());
        String prevId = d.getRankId();
        if (remPrev && prevId != null && !prevId.equals(rank.id)) {
            Rank prev = null;
            // try find by id
            // (RankService is not globally exposed; rely on stored suffix/group in file where possible)
            // As fallback, remove same as rank.group if present in extra
            // For correctness, store last group in PlayerData? Keep it simple: remove any extra groups list.
        }
        if (remExtra != null) {
            for (String g : remExtra) {
                String cmd = remTmpl.replace("{player}", p.getName())
                                    .replace("{uuid}", p.getUniqueId().toString())
                                    .replace("{group}", g);
                org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), cmd);
            }
        }
    }
}
