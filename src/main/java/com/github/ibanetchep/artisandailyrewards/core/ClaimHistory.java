package com.github.ibanetchep.artisandailyrewards.core;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A player's recent counted days, read at join and kept up to date by each check-in and claim:
 * what the calendar menu shows of the past, without I/O.
 *
 * @param firstDay  game day the player was first counted, null before it: the days before it are
 *                  not missed, the player was not there yet
 * @param days      game day to the day of the cycle it pays, for the recent days only
 * @param unclaimed the counted days whose reward waits to be claimed, all of them however old
 */
public record ClaimHistory(@Nullable Long firstDay, NavigableMap<Long, Integer> days, NavigableSet<Long> unclaimed) {

    public static final ClaimHistory EMPTY = new ClaimHistory(null, new TreeMap<>(), new TreeSet<>());

    public ClaimHistory {
        days = Collections.unmodifiableNavigableMap(new TreeMap<>(days));
        unclaimed = Collections.unmodifiableNavigableSet(new TreeSet<>(unclaimed));
    }

    public static ClaimHistory of(@Nullable Long firstDay, Map<Long, Integer> days) {
        return of(firstDay, days, Set.of());
    }

    public static ClaimHistory of(@Nullable Long firstDay, Map<Long, Integer> days, Collection<Long> unclaimed) {
        return new ClaimHistory(firstDay, new TreeMap<>(days), new TreeSet<>(unclaimed));
    }

    /** The day of the cycle {@code day} pays, or null when it was not counted (or is not known). */
    public @Nullable Integer cycleDayOn(long day) {
        return days.get(day);
    }

    /** {@code day} counted and its reward not claimed yet. */
    public boolean unclaimedOn(long day) {
        return unclaimed.contains(day);
    }

    /** With {@code day} counted, its reward waiting. */
    public ClaimHistory with(long day, int cycleDay) {
        TreeMap<Long, Integer> next = new TreeMap<>(days);
        next.put(day, cycleDay);
        TreeSet<Long> waiting = new TreeSet<>(unclaimed);
        waiting.add(day);
        return new ClaimHistory(firstDay == null ? day : Math.min(firstDay, day), next, waiting);
    }

    /** With the rewards of {@code claimedDays} claimed. */
    public ClaimHistory claimed(Collection<Long> claimedDays) {
        TreeSet<Long> waiting = new TreeSet<>(unclaimed);
        waiting.removeAll(claimedDays);
        return new ClaimHistory(firstDay, days, waiting);
    }
}
