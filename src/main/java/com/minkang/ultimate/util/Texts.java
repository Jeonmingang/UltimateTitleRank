
package com.minkang.ultimate.util;

import org.bukkit.ChatColor;

public class Texts {
    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
    public static String nullTo(String s, String def) {
        return s == null ? def : s;
    }
}
