package com.minkang.ultimate.autorank;

public final class TimeUtil {
    private TimeUtil() {}

    public static String formatSeconds(long seconds) {
        if (seconds < 0) seconds = 0;
        long days = seconds / 86400;
        long rem = seconds % 86400;
        long hours = rem / 3600;
        rem %= 3600;
        long minutes = rem / 60;
        long secs = rem % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(secs).append("s");
        return sb.toString().trim();
    }
}