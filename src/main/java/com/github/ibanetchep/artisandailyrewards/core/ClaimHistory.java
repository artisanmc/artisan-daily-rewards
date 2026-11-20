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
 * @param unclaimed  the counted days whose reward waits to be claimed, all of them however old
 * @param milestones the days of {@code days} that also pay milestones, to the streak they reached
 * @param streaks    the days of {@code days} to the streak each one reached, where known: a day
 *                   missed before a day whose streak is above 1 was covered by a freeze
 */
public record ClaimHistory(@Nullable Long firstDay, NavigableMap<Long, Integer> days, NavigableSet<Long> unclaimed,
                           NavigableMap<Long, Integer> milestones, NavigableMap<Long, Integer> streaks) {

    public static final ClaimHistory EMPTY = new ClaimHistory(null, new TreeMap<>(), new TreeSet<>(), new TreeMap<>(), new TreeMap<>());

    public ClaimHistory {
        days = Collections.unmodifiableNavigableMap(new TreeMap<>(days));
        unclaimed = Collections.unmodifiableNavigableSet(new TreeSet<>(unclaimed));
        milestones = Collections.unmodifiableNavigableMap(new TreeMap<>(milestones));
        streaks = Collections.unmodifiableNavigableMap(new TreeMap<>(streaks));
    }

    public ClaimHistory(@Nullable Long firstDay, NavigableMap<Long, Integer> days, NavigableSet<Long> unclaimed,
                        NavigableMap<Long, Integer> milestones) {
        this(firstDay, days, unclaimed, milestones, new TreeMap<>());
    }

    public static ClaimHistory of(@Nullable Long firstDay, Map<Long, Integer> days) {
        return of(firstDay, days, Set.of());
    }

    public static ClaimHistory of(@Nullable Long firstDay, Map<Long, Integer> days, Collection<Long> unclaimed) {
        return of(firstDay, days, unclaimed, Map.of());
    }

    public static ClaimHistory of(@Nullable Long firstDay, Map<Long, Integer> days, Collection<Long> unclaimed,
                                  Map<Long, Integer> milestones) {
        return new ClaimHistory(firstDay, new TreeMap<>(days), new TreeSet<>(unclaimed), new TreeMap<>(milestones));
    }

    /** As the others, with the streak each counted day reached. */
    public static ClaimHistory of(@Nullable Long firstDay, Map<Long, Integer> days, Collection<Long> unclaimed,
                                  Map<Long, Integer> milestones, Map<Long, Integer> streaks) {
        return new ClaimHistory(firstDay, new TreeMap<>(days), new TreeSet<>(unclaimed), new TreeMap<>(milestones),
                new TreeMap<>(streaks));
    }

    /**
     * {@code day} was not counted, and a freeze covered it: the next counted day continued the
     * streak (its streak is above 1). False when that day is not known.
     */
    public boolean coveredOn(long day) {
        if (days.containsKey(day)) return false;
        Map.Entry<Long, Integer> next = streaks.ceilingEntry(day);
        if (next == null) return false;
        Long counted = days.ceilingKey(day);
        return counted != null && counted.equals(next.getKey()) && next.getValue() > 1;
    }

    /** The streak {@code day} reached when it pays milestones on top of its reward, else null. */
    public @Nullable Integer milestoneOn(long day) {
        return milestones.get(day);
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
        return with(day, cycleDay, 0);
    }

    /**
     * With {@code day} counted, its reward waiting, and its milestones with it when
     * {@code milestoneStreak} (the streak it reached) is above 0.
     */
    public ClaimHistory with(long day, int cycleDay, int milestoneStreak) {
        return with(day, cycleDay, milestoneStreak, 0);
    }

    /** As {@link #with(long, int, int)}, with the streak {@code day} reached when above 0. */
    public ClaimHistory with(long day, int cycleDay, int milestoneStreak, int streak) {
        TreeMap<Long, Integer> next = new TreeMap<>(days);
        next.put(day, cycleDay);
        TreeSet<Long> waiting = new TreeSet<>(unclaimed);
        waiting.add(day);
        TreeMap<Long, Integer> bonus = new TreeMap<>(milestones);
        if (milestoneStreak > 0) bonus.put(day, milestoneStreak);
        else bonus.remove(day);
        TreeMap<Long, Integer> reached = new TreeMap<>(streaks);
        if (streak > 0) reached.put(day, streak);
        else reached.remove(day);
        return new ClaimHistory(firstDay == null ? day : Math.min(firstDay, day), next, waiting, bonus, reached);
    }

    /** With the rewards of {@code claimedDays} claimed. */
    public ClaimHistory claimed(Collection<Long> claimedDays) {
        TreeSet<Long> waiting = new TreeSet<>(unclaimed);
        waiting.removeAll(claimedDays);
        return new ClaimHistory(firstDay, days, waiting, milestones, streaks);
    }
}
