package com.minkang.ultimate.titles.util;

import org.bukkit.ChatColor;

public class ColorUtil {
    public static String colorize(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    
    public static String decodeHiddenKey(org.bukkit.inventory.ItemStack it) {
        if (it == null) return "";
        org.bukkit.inventory.meta.ItemMeta im = it.getItemMeta();
        if (im == null) return "";
        java.util.List<String> lore = im.getLore();
        if (lore == null) return "";
        for (String l : lore) {
            if (org.bukkit.ChatColor.stripColor(l).startsWith("KEY:")) {
                return org.bukkit.ChatColor.stripColor(l).substring(4);
            }
        }
        return "";
    }

}

    public static String decodeHiddenKey(org.bukkit.inventory.ItemStack it) {
        if (it == null) return "";
        org.bukkit.inventory.meta.ItemMeta im = it.getItemMeta();
        if (im == null) return "";
        java.util.List<String> lore = im.getLore();
        if (lore == null) return "";
        for (String l : lore) {
            if (org.bukkit.ChatColor.stripColor(l).startsWith("KEY:")) {
                return org.bukkit.ChatColor.stripColor(l).substring(4);
            }
        }
        return "";
    }

}
