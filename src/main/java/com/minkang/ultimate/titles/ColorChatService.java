package com.minkang.ultimate.titles;

import org.bukkit.Material;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ColorChatService {
    private final Main plugin;
    private final Map<String, ColorDef> colors = new HashMap<>();
    private final Map<String, String> nameToId = new HashMap<>();

    public ColorChatService(Main plugin) {
        this.plugin = plugin;
        load();
    }

    @SuppressWarnings("unchecked")
    public void load() {
        colors.clear();
        nameToId.clear();
        List<Map<?, ?>> list = (List<Map<?, ?>>) plugin.getConfig().getList("colorchat.colors");
        if (list != null) {
            for (Map<?, ?> m : list) {
                try {
                    String id = String.valueOf(m.get("id")).toLowerCase();
                    String name = String.valueOf(m.get("name"));
                    String code = String.valueOf(m.get("code"));
                    String mat = String.valueOf(m.get("material"));
                    Material material;
                    try { material = Material.valueOf(mat); } catch (Exception e) { material = Material.NAME_TAG; }
                    ColorDef def = new ColorDef(id, name, code, material);
                    colors.put(id, def);
                    nameToId.put(name, id);
                } catch (Exception ignored) { }
            }
        }
    }

    public Collection<ColorDef> all() { return colors.values(); }

    public ColorDef get(String id) { return id == null ? null : colors.get(id.toLowerCase()); }

    public ColorDef getByNameOrId(String token) {
        if (token == null) return null;
        ColorDef d = get(token);
        if (d != null) return d;
        String id = nameToId.get(token);
        return id == null ? null : get(id);
    }

    public void grant(UUID uuid, String colorId) {
        PlayerData d = plugin.storage().get(uuid);
        if (colorId != null) d.getColorsOwned().add(colorId.toLowerCase());
        plugin.storage().save(uuid, d);
    }

    public void revoke(UUID uuid, String colorId) {
        PlayerData d = plugin.storage().get(uuid);
        if (colorId != null) d.getColorsOwned().remove(colorId.toLowerCase());
        if (colorId != null && colorId.equalsIgnoreCase(d.getActiveColorId())) d.setActiveColorId(null);
        plugin.storage().save(uuid, d);
    }

    public void setActive(UUID uuid, String colorId) {
        PlayerData d = plugin.storage().get(uuid);
        String id = colorId == null ? null : colorId.toLowerCase();
        if (id != null && !d.getColorsOwned().contains(id)) return;
        d.setActiveColorId(id);
        plugin.storage().save(uuid, d);
    }
}
