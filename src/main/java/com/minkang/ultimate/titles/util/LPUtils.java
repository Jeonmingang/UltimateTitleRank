package com.minkang.ultimate.titles.util;

import com.minkang.ultimate.titles.UltimateTitleRank;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class LPUtils {
    private final UltimateTitleRank plugin;
    private LuckPerms api;

    public LPUtils(UltimateTitleRank plugin) {
        this.plugin = plugin;
        try { this.api = LuckPermsProvider.get(); } catch (IllegalStateException ex) { this.api = null; }
    }

    public boolean isAvailable() { return api != null; }

    public void setPrimaryGroup(Player player, String groupName, String oldGroup) {
        if (api == null) return;
        final String target = groupName;
        final String prev = oldGroup == null ? "" : oldGroup;
        CompletableFuture<User> fu = api.getUserManager().loadUser(player.getUniqueId());
        fu.thenAccept(new java.util.function.Consumer<User>() {
            @Override
            public void accept(User user) {
                if (!prev.isEmpty()) {
                    Node rem = InheritanceNode.builder(prev).build();
                    user.data().remove(rem);
                }
                Node add = InheritanceNode.builder(target).build();
                user.data().add(add);
                api.getUserManager().saveUser(user);
            }
        });
    }
}
