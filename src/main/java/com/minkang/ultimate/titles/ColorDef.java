package com.minkang.ultimate.titles;

import org.bukkit.Material;

public class ColorDef {
    public final String id;
    public final String name;
    public final String code;
    public final Material material;
    public ColorDef(String id, String name, String code, Material material) {
        this.id=id; this.name=name; this.code=code; this.material=material;
    }
}
