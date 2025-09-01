
package com.minkang.ultimate.managers;

import com.minkang.ultimate.UltimateTitleRank;
import com.minkang.ultimate.util.Texts;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RankManager {
    private final UltimateTitleRank plugin;
    private LuckPerms luckPerms;
    private int taskId = -1;

    // map current->(next, seconds)
    private static class Chain {
        final String current;
        final String next;
        final long seconds;
        Chain(String c, String n, long s) { current = c; next = n; seconds = s; }
    }
    private final List<Chain> chains = new ArrayList<>();

    public RankManager(UltimateTitleRank plugin) {
        this.plugin = plugin;
        try {
            this.luckPerms = LuckPermsProvider.get();
            plugin.getLogger().info("LuckPerms API 연결 성공");
        } catch (IllegalStateException e) {
            this.luckPerms = null;
            plugin.getLogger().warning("LuckPerms API를 찾지 못했습니다. 자동 승급이 비활성화됩니다.");
        }
    }

    public void load() {
        chains.clear();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("ranks.chain");
        if (sec == null) {
            // when using list form
            List<Map<?, ?>> list = plugin.getConfig().getMapList("ranks.chain");
            for (Map<?, ?> m : list) {
                String current = String.valueOf(m.get("current"));
                String next = String.valueOf(m.get("next"));
                long seconds = Long.parseLong(String.valueOf(m.get("time_seconds")));
                chains.add(new Chain(current, next, seconds));
            }
        } else {
            for (String key : sec.getKeys(false)) {
                ConfigurationSection c = sec.getConfigurationSection(key);
                if (c == null) continue;
                chains.add(new Chain(
                        c.getString("current","default"),
                        c.getString("next","member"),
                        c.getLong("time_seconds",3600L)
                ));
            }
        }
    }

    public void startScheduler() {
        int interval = plugin.getConfig().getInt("rankup.check_interval_seconds", 60);
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                tryPromote(p);
            }
        }, interval * 20L, interval * 20L);
    }

    public void shutdown() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
    }

    public long getPlaySeconds(Player p) {
        // 기본: Bukkit Statistic
        int ticks = p.getStatistic(Statistic.PLAY_ONE_MINUTE); // ticks
        long seconds = ticks / 20L;
        return Math.max(0L, seconds);
    }

    public String getPrimaryGroup(User user) {
        // LuckPerms primary group
        return user.getPrimaryGroup();
    }

    public Optional<Chain> findChainByCurrent(String currentGroup) {
        for (Chain c : chains) {
            if (c.current.equalsIgnoreCase(currentGroup)) return Optional.of(c);
        }
        return Optional.empty();
    }

    public void tryPromote(Player p) {
        if (luckPerms == null) return;
        CompletableFuture<User> fut = luckPerms.getUserManager().loadUser(p.getUniqueId());
        fut.thenAccept(user -> {
            String current = getPrimaryGroup(user);
            Optional<Chain> opt = findChainByCurrent(current);
            if (!opt.isPresent()) return;

            Chain chain = opt.get();
            long seconds = getPlaySeconds(p);
            if (seconds >= chain.seconds) {
                // promote
                Group nextGroup = luckPerms.getGroupManager().getGroup(chain.next);
                if (nextGroup == null) {
                    plugin.getLogger().warning("LuckPerms 그룹을 찾을 수 없음: " + chain.next);
                    return;
                }

                // set primary group and add inheritance node
                user.setPrimaryGroup(chain.next);
                InheritanceNode node = InheritanceNode.builder(chain.next).build();
                user.data().add(node);
                luckPerms.getUserManager().saveUser(user);

                String msg = plugin.getConfig().getString("rankup.broadcast_message",
                        "&6%player%&e 님이 &b%next% &e등급으로 자동 승급되었습니다! 축하합니다!"
                );
                msg = msg.replace("%player%", p.getName()).replace("%next%", chain.next);
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcastMessage(Texts.color(msg)));
            }
        });
    }

    public String formatRankInfo(Player p) {
        if (luckPerms == null) return Texts.color("&cLuckPerms가 감지되지 않아 승급 정보를 표시할 수 없습니다.");
        User user = luckPerms.getUserManager().getUser(p.getUniqueId());
        if (user == null) {
            return Texts.color("&c잠시 후 다시 시도해주세요.");
        }
        String current = getPrimaryGroup(user);
        Optional<Chain> opt = findChainByCurrent(current);
        long played = getPlaySeconds(p);
        if (!opt.isPresent()) {
            return Texts.color("&e현재 그룹: &a" + current + " &7(승급 체인에 없음)\n&7누적 플레이시간: &f" + played + "초");
        }
        Chain chain = opt.get();
        long need = Math.max(0, chain.seconds - played);
        return Texts.color("&e현재 그룹: &a" + current + "  &e다음 그룹: &b" + chain.next +
                "\n&7누적 플레이시간: &f" + played + "초 &7/ 필요: &f" + chain.seconds + "초" +
                "\n&7남은 시간: &f" + need + "초");
    }
}
