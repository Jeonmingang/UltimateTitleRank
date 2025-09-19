
package com.minkang.ultimate.titles;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.SuffixNode;
import org.bukkit.entity.Player;

public class LuckPermsHook {
    private LuckPerms api;

    private LuckPerms getApi() {
        if (api != null) return api;
        try { api = LuckPermsProvider.get(); return api; } catch (Throwable t) { return null; }
    }

    public void applyRank(Player p, Rank rank) {
        LuckPerms lp = getApi();
        if (lp == null) return;
        lp.getUserManager().loadUser(p.getUniqueId()).thenAcceptAsync((User user) -> {
            try {
                if (rank.group != null && !rank.group.isEmpty()) {
                    user.data().add(Node.builder("group." + rank.group).build());
                }
                if (rank.suffix != null && !rank.suffix.isEmpty()) {
                    SuffixNode node = SuffixNode.builder(Chat.color(rank.suffix), 10).build();
                    user.data().add(node);
                }
                lp.getUserManager().saveUser(user);
            } catch (Exception ignored) {}
        });
    }
}
