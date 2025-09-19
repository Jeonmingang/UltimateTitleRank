
package com.minkang.ultimate.titles;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TitleService {
    private final Main plugin;
    private final Map<Integer, String> generatedTitles = new HashMap<>();

    public TitleService(Main plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        TitlesFile.reload(plugin);
        generatedTitles.clear();
        generatedTitles.putAll(TitlesFile.getAll());
    }

    public Map<Integer, String> getGeneratedTitles() { return Collections.unmodifiableMap(generatedTitles); }

    public void createTitle(int slot, String name) {
        generatedTitles.put(slot, name);
        TitlesFile.set(slot, name);
        TitlesFile.save();
    }

    public void deleteTitle(int slot) {
        generatedTitles.remove(slot);
        TitlesFile.remove(slot);
        TitlesFile.save();
    }

    public void applyTitle(Player p, String rawTitle) {
        String colored = Chat.color(rawTitle);
        plugin.luck().applyRank(p, new Rank("custom", colored, 0, null, colored, 0, 0));
        p.sendMessage(Chat.color(plugin.msg("prefix") + plugin.msg("apply.success").replace("{title}", colored)));
    }
}
