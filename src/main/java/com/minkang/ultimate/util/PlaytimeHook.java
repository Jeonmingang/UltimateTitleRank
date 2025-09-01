
package com.minkang.ultimate.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaytimeHook {

    // Common placeholders we will try in order. (seconds preferred)
    private static final String[] CANDIDATE_PLACEHOLDERS = new String[]{
            "%playtime_time_seconds%",
            "%playtime_seconds%",
            "%player_time_seconds%",
            "%PlayTime_seconds%",
            "%playtime_time%",
            "%player_time%",
            "%PlayTime_time%"
    };

    /**
     * Try to get seconds from PlaceholderAPI/Playtime. Falls back to 0 if unavailable.
     */
    public static long getPlaySeconds(Player p) {
        try {
            if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) return 0L;
            Class<?> papi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Method setPlaceholders = papi.getMethod("setPlaceholders", Player.class, String.class);

            for (String ph : CANDIDATE_PLACEHOLDERS) {
                Object out = setPlaceholders.invoke(null, p, ph);
                if (out == null) continue;
                String s = String.valueOf(out).trim();
                long parsed = parseDurationToSeconds(s);
                if (parsed > 0) return parsed;
            }
        } catch (Throwable ignored) { }
        return 0L;
    }

    /**
     * Parse strings like "1d 2h 3m 4s", "02:03:04", or plain digits (seconds).
     */
    public static long parseDurationToSeconds(String s) {
        if (s == null || s.isEmpty()) return 0L;
        s = s.trim();

        // plain digits => seconds
        if (s.matches("^\\d+$")) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignored) {}
        }

        // HH:MM:SS (or MM:SS)
        if (s.matches("^\\d{1,2}:\\d{1,2}(:\\d{1,2})?$")) {
            String[] parts = s.split(":");
            try {
                if (parts.length == 2) {
                    long mm = Long.parseLong(parts[0]);
                    long ss = Long.parseLong(parts[1]);
                    return mm * 60 + ss;
                } else if (parts.length == 3) {
                    long hh = Long.parseLong(parts[0]);
                    long mm = Long.parseLong(parts[1]);
                    long ss = Long.parseLong(parts[2]);
                    return hh * 3600 + mm * 60 + ss;
                }
            } catch (NumberFormatException ignored) {}
        }

        // "1d 2h 3m 4s" style
        long total = 0L;
        Pattern p = Pattern.compile("(\\d+)\\s*([dhmsDHMS])");
        Matcher m = p.matcher(s);
        boolean matched = false;
        while (m.find()) {
            matched = true;
            long val = Long.parseLong(m.group(1));
            char unit = Character.toLowerCase(m.group(2).charAt(0));
            switch (unit) {
                case 'd': total += val * 86400L; break;
                case 'h': total += val * 3600L; break;
                case 'm': total += val * 60L; break;
                case 's': total += val; break;
            }
        }
        return matched ? total : 0L;
    }
}
