package com.minkang.ultimate.playtimetitles.managers;

import com.minkang.ultimate.playtimetitles.Main;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.SuffixNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LuckPermsBridge {
    private final Main plugin;
    private LuckPerms api;
    private boolean available = false;

    public LuckPermsBridge(Main plugin) {
        this.plugin = plugin;
        try {
            this.api = LuckPermsProvider.get();
            available = (this.api != null);
        } catch (Throwable t) {
            available = false;
        }
    }

    public boolean isAvailable() { return available; }

    public void setSuffix(Player p, String suffixColored) {
        if (!available) return;
        try {
            User user = api.getUserManager().getUser(p.getUniqueId());
            if (user == null) return;
            // remove existing suffix nodes at a default priority (100)
            user.getNodes().stream()
                    .filter(n -> NodeType.SUFFIX.matches(n))
                    .forEach(n -> user.data().remove(n));
            SuffixNode node = SuffixNode.builder(suffixColored, 100).build();
            user.data().add(node);
            api.getUserManager().saveUser(user);
        } catch (Throwable t) {
            plugin.getLogger().warning("LP setSuffix 실패: " + t.getMessage());
        }
    }

    public void clearSuffix(Player p) {
        if (!available) return;
        try {
            User user = api.getUserManager().getUser(p.getUniqueId());
            if (user == null) return;
            user.getNodes().stream()
                    .filter(n -> NodeType.SUFFIX.matches(n))
                    .forEach(n -> user.data().remove(n));
            api.getUserManager().saveUser(user);
        } catch (Throwable t) {
            plugin.getLogger().warning("LP clearSuffix 실패: " + t.getMessage());
        }
    }

    public void addGroup(Player p, String group) {
        if (!available) return;
        try {
            User user = api.getUserManager().getUser(p.getUniqueId());
            if (user == null) return;
            InheritanceNode node = InheritanceNode.builder(group).build();
            user.data().add(node);
            api.getUserManager().saveUser(user);
        } catch (Throwable t) {
            plugin.getLogger().warning("LP addGroup 실패: " + t.getMessage());
        }
    }

    public void removeGroup(Player p, String group) {
        if (!available) return;
        try {
            User user = api.getUserManager().getUser(p.getUniqueId());
            if (user == null) return;
            InheritanceNode node = InheritanceNode.builder(group).build();
            user.data().remove(node);
            api.getUserManager().saveUser(user);
        } catch (Throwable t) {
            plugin.getLogger().warning("LP removeGroup 실패: " + t.getMessage());
        }
    }
}
