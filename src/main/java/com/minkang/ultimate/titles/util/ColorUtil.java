package com.minkang.ultimate.titles.util;

import org.bukkit.ChatColor;

public class ColorUtil {
    public static String colorize(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
