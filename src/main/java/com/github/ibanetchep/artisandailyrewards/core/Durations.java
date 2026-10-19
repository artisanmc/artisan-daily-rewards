package com.github.ibanetchep.artisandailyrewards.core;

import java.time.Duration;

/** Short durations for chat: {@code 3h 12m}, {@code 45m}, {@code 30s}. */
public final class Durations {

    private Durations() {
    }

    public static String format(Duration duration) {
        long seconds = Math.max(0, duration.getSeconds());
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0) return minutes > 0 ? hours + "h " + minutes + "m" : hours + "h";
        if (minutes > 0) return minutes + "m";
        return Math.max(1, seconds) + "s";
    }
}
