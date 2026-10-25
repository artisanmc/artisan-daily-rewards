package com.github.ibanetchep.artisandailyrewards.core;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A player's recent claims, read at join and kept up to date by each claim: what the calendar
 * menu shows of the past, without I/O.
 *
 * @param firstDay game day of the player's first claim ever, null before it: the days before it
 *                 are not missed, the player was not there yet
 * @param days     game day to the day of the cycle that claim paid, for the recent days only
 */
public record ClaimHistory(@Nullable Long firstDay, NavigableMap<Long, Integer> days) {

    public static final ClaimHistory EMPTY = new ClaimHistory(null, new TreeMap<>());

    public ClaimHistory {
        days = Collections.unmodifiableNavigableMap(new TreeMap<>(days));
    }

    public static ClaimHistory of(@Nullable Long firstDay, Map<Long, Integer> days) {
        return new ClaimHistory(firstDay, new TreeMap<>(days));
    }

    /** The day of the cycle claimed on {@code day}, or null when nothing is known of that day. */
    public @Nullable Integer cycleDayOn(long day) {
        return days.get(day);
    }

    /** With the claim of {@code day} added. */
    public ClaimHistory with(long day, int cycleDay) {
        TreeMap<Long, Integer> next = new TreeMap<>(days);
        next.put(day, cycleDay);
        return new ClaimHistory(firstDay == null ? day : Math.min(firstDay, day), next);
    }
}
