package com.minkang.ultimate.autorank;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PromotionManager {

    private final UltimateAutoRankPlugin plugin;
    private LuckPerms luckPerms;
    private final List<RankRule> rules;
    private final String playtimePlaceholder;
    private final String msgPrefix;
    private final String msgPromoted;
    private final boolean papiReady;

    public PromotionManager(UltimateAutoRankPlugin plugin) {
        this.plugin = plugin;

        try {
            this.luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            this.luckPerms = null;
        }

        FileConfiguration cfg = plugin.getConfig();
        this.playtimePlaceholder = cfg.getString("playtime-seconds-placeholder", "%playtime_seconds_all%");
        this.msgPrefix = color(cfg.getString("messages.prefix", "&7[&a승급&7]&r "));
        this.msgPromoted = color(cfg.getString("messages.promoted", "&a축하합니다! &e%new_group% &a등급으로 승급되었습니다."));

        // rules
        List<RankRule> list = new ArrayList<RankRule>();
        List<Map<?, ?>> raw = cfg.getMapList("ranks");
        for (Map<?, ?> entry : raw) {
            if (entry == null) continue;
            String name = String.valueOf(entry.get("name"));
            String promoteTo = String.valueOf(entry.get("promote-to"));
            long required = 0L;
            Object v = entry.get("required-seconds");
            if (v instanceof Number) {
                required = ((Number) v).longValue();
            } else if (v != null) {
                try {
                    required = Long.parseLong(String.valueOf(v));
                } catch (NumberFormatException ignore) {}
            }
            List<String> remove = new ArrayList<String>();
            Object rg = entry.get("remove-groups");
            if (rg instanceof List) {
                for (Object o : (List<?>) rg) {
                    if (o != null) remove.add(String.valueOf(o));
                }
            }
            if (promoteTo != null && promoteTo.length() > 0 && required > 0) {
                list.add(new RankRule(name, promoteTo, required, remove));
            }
        }
        Collections.sort(list, new Comparator<RankRule>() {
            @Override public int compare(RankRule o1, RankRule o2) {
                return Long.compare(o1.requiredSeconds, o2.requiredSeconds);
            }
        });
        this.rules = Collections.unmodifiableList(list);

        boolean hasPAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        boolean hasPlaytime = Bukkit.getPluginManager().getPlugin("Playtime") != null;
        this.papiReady = hasPAPI && hasPlaytime;
    }

    public boolean isReady() {
        return luckPerms != null && papiReady;
    }

    public void tickOnlinePlayers() {
        if (!isReady()) return;
        for (Player p : Bukkit.getOnlinePlayers()) {
            tryPromote(p);
        }
    }

    public void tryPromote(Player player) {
        if (player == null) return;
        long seconds = resolvePlaytimeSeconds(player);
        if (seconds < 0) return;

        // Determine highest eligible rule not yet owned
        List<String> currentGroups = getCurrentPrimaryAndInheritance(player);
        RankRule nextEligible = null;
        for (RankRule rule : rules) {
            boolean ok = seconds >= rule.requiredSeconds;
            if (!ok) continue;
            boolean already = currentGroups.contains(rule.promoteTo);
            if (!already) {
                nextEligible = rule;
            }
        }
        if (nextEligible == null) return;

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            final RankRule ruleToApply = nextEligible;
            final java.util.UUID uuid = player.getUniqueId();
            luckPerms.getUserManager().loadUser(uuid).thenAccept(new java.util.function.Consumer<User>() {
                @Override
                public void accept(final User u) {
                    if (u != null) {
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                Player pOnline = Bukkit.getPlayer(uuid);
                                applyPromotion(pOnline != null ? pOnline : player, u, ruleToApply);
                            }
                        });
                    }
                }
            });
            return;
        }
        applyPromotion(player, user, nextEligible);
    }

    private void applyPromotion(Player player, User user, RankRule rule) {
        if (user == null || rule == null) return;

        // Remove groups first
        if (rule.removeGroups != null) {
            for (String g : rule.removeGroups) {
                if (g == null || g.isEmpty()) continue;
                InheritanceNode node = InheritanceNode.builder(g).build();
                if (!user.data().remove(node)) {
                    Node legacy = Node.builder("group." + g).build();
                    user.data().remove(legacy);
                }
            }
        }
        // Add target group
        InheritanceNode add = InheritanceNode.builder(rule.promoteTo).build();
        user.data().add(add);
        luckPerms.getUserManager().saveUser(user);

        if (player != null) {
            String msg = msgPrefix + msgPromoted.replace("%new_group%", rule.promoteTo);
            player.sendMessage(msg);
        }
    }

    private List<String> getCurrentPrimaryAndInheritance(Player p) {
        if (p == null) return Collections.emptyList();
        User u = luckPerms.getUserManager().getUser(p.getUniqueId());
        if (u == null) return Collections.emptyList();
        Set<String> groups = new HashSet<String>();
        if (u.getPrimaryGroup() != null) groups.add(u.getPrimaryGroup());
        for (net.luckperms.api.node.Node n : u.getNodes()) {
            if (n instanceof InheritanceNode) {
                groups.add(((InheritanceNode) n).getGroupName());
            }
        }
        return new ArrayList<String>(groups);
    }

    public java.util.List<String> getCurrentGroups(Player p) {
        return getCurrentPrimaryAndInheritance(p);
    }

    public long resolvePlaytimeSeconds(Player player) {
        try {
            Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            java.lang.reflect.Method m = papi.getMethod("setPlaceholders", org.bukkit.OfflinePlayer.class, String.class);
            Object result = m.invoke(null, player, this.playtimePlaceholder);
            String str = String.valueOf(result).trim();
            long value;
            try {
                value = Long.parseLong(str.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ex) {
                return -1L;
            }
            return value;
        } catch (Exception e) {
            return -1L;
        }
    }

    private static String color(String s) {
        return s == null ? "" : org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }

    public RankRule getNextRuleFor(long seconds, Collection<String> currentGroups) {
        if (currentGroups == null) currentGroups = Collections.emptyList();
        for (RankRule rule : rules) {
            if (seconds < rule.requiredSeconds && !currentGroups.contains(rule.promoteTo)) {
                return rule;
            }
        }
        return null;
    }

    public static final class RankRule {
        public final String name;
        public final String promoteTo;
        public final long requiredSeconds;
        public final List<String> removeGroups;

        public RankRule(String name, String promoteTo, long requiredSeconds, List<String> removeGroups) {
            this.name = name;
            this.promoteTo = promoteTo;
            this.requiredSeconds = requiredSeconds;
            this.removeGroups = removeGroups;
        }
    }
}
