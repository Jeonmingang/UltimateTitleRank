
package com.minkang.ultimate.titles;

import org.bukkit.ChatColor;

public class Chat {
    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
