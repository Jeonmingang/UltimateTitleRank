package com.minkang.ultimate.autorank;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
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

        this.playtimePlaceholder = plugin.getConfig().getString("playtime-seconds-placeholder", "%playtime_seconds_all%");
        this.msgPrefix = color(plugin.getConfig().getString("messages.prefix", "&7[&a승급&7]&r "));
        this.msgPromoted = color(plugin.getConfig().getString("messages.promoted", "&a축하합니다! &e%new_group% &a등급으로 승급되었습니다."));

        // Load rules
        List<RankRule> list = new ArrayList<>();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("ranks");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                // Not used because ranks is a list,
                // but guard in case user changed to section.
            }
        }
        List<Map<?, ?>> raw = plugin.getConfig().getMapList("ranks");
        for (Map<?, ?> entry : raw) {
            String name = String.valueOf(entry.get("name"));
            String promoteTo = String.valueOf(entry.get("promote-to"));
            long required = ((Number) entry.get("required-seconds")).longValue();
            List<String> remove = new ArrayList<>();
            Object rg = entry.get("remove-groups");
            if (rg instanceof List) {
                for (Object o : (List<?>) rg) {
                    remove.add(String.valueOf(o));
                }
            }
            list.add(new RankRule(name, promoteTo, required, remove));
        }
        // sort ascending by required seconds
        list.sort(Comparator.comparingLong(r -> r.requiredSeconds));
        this.rules = Collections.unmodifiableList(list);

        // Check dependencies
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
        long seconds = resolvePlaytimeSeconds(player);
        if (seconds < 0) return;

        // Determine highest eligible rule not yet owned
        List<String> currentGroups = getCurrentPrimaryAndInheritance(player);
        RankRule nextEligible = null;
        for (RankRule rule : rules) {
            if (seconds >= rule.requiredSeconds && !currentGroups.contains(rule.promoteTo)) {
                nextEligible = rule;
            }
        }
        if (nextEligible == null) return;

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            // Likely loaded for online players, but guard
            luckPerms.getUserManager().loadUser(player.getUniqueId()).thenAccept(u -> {
                if (u != null) {
                    applyPromotion(player, u, nextEligible);
                }
            });
            return;
        }
        applyPromotion(player, user, nextEligible);
    }

    private void applyPromotion(Player player, User user, RankRule rule) {
        // Remove groups first
        if (rule.removeGroups != null) {
            for (String g : rule.removeGroups) {
                if (g == null || g.isEmpty()) {
                    continue;
                }
                InheritanceNode node = InheritanceNode.builder(g).build();
                if (user.data().remove(node)) {
                    // removed
                } else {
                    // try removing generic "group." node (older API compat)
                    Node legacy = Node.builder("group." + g).build();
                    user.data().remove(legacy);
                }
            }
        }
        // Add target group
        InheritanceNode add = InheritanceNode.builder(rule.promoteTo).build();
        user.data().add(add);
        luckPerms.getUserManager().saveUser(user);

        String msg = msgPrefix + msgPromoted.replace("%new_group%", rule.promoteTo);
        player.sendMessage(msg);
    }

    private List<String> getCurrentPrimaryAndInheritance(Player p) {
        User u = luckPerms.getUserManager().getUser(p.getUniqueId());
        if (u == null) return Collections.emptyList();
        Set<String> groups = new HashSet<>();
        // primary group
        if (u.getPrimaryGroup() != null) groups.add(u.getPrimaryGroup());
        // inheritance nodes
        u.getNodes().stream()
                .filter(n -> n instanceof InheritanceNode)
                .map(n -> ((InheritanceNode) n).getGroupName())
                .forEach(groups::add);
        return groups.stream().collect(Collectors.toList());
    }

    public long resolvePlaytimeSeconds(Player player) {
        // PlaceholderAPI via reflection to avoid hard dependency
        try {
            Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            java.lang.reflect.Method m = papi.getMethod("setPlaceholders", org.bukkit.OfflinePlayer.class, String.class);
            Object result = m.invoke(null, player, this.playtimePlaceholder);
            String str = String.valueOf(result).trim();
            // Allow formatted values like "12345" only
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
        for (RankRule rule : rules) {
            if (seconds < rule.requiredSeconds) {
                // Next target not yet reached
                if (!currentGroups.contains(rule.promoteTo)) {
                    return rule;
                }
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