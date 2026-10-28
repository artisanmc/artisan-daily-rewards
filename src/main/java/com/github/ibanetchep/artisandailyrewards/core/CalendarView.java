package com.github.ibanetchep.artisandailyrewards.core;

import java.util.ArrayList;
import java.util.List;

/**
 * The calendar a player sees, pure: the recent game days as they went (claimed, waiting to be
 * claimed, missed), today, and the days to come with the day of the cycle each one will pay if the
 * player comes every day from now on.
 */
public final class CalendarView {

    private CalendarView() {
    }

    public enum Status {
        /** Before the player was first counted: not missed, they were not there yet. */
        BEFORE,
        /** Counted and its reward claimed, today or before. */
        CLAIMED,
        /** Over, and the player was not there. A joker may have saved the streak; the reward is lost either way. */
        MISSED,
        /**
         * Its reward waits to be claimed: today, or a day before when the player was there without
         * claiming. A claim pays every such day.
         */
        AVAILABLE,
        /** A day to come. */
        UPCOMING
    }

    /**
     * @param offset   days from today: negative in the past, 0 today
     * @param cycleDay the day of the cycle paid (claimed) or to be paid (available, upcoming); 0
     *                 for a day missed or before the first one
     */
    public record Day(long day, int offset, Status status, int cycleDay) {
    }

    /**
     * {@code past} days before today, today, then {@code future} days after it. The past comes
     * from {@code history}: a day it does not hold is missed, unless it is before the first one.
     * Today and the future follow the streak as {@link StreakRules#display} shows it: a broken
     * streak starts again at day 1, one the jokers still cover goes on.
     */
    public static List<Day> around(PlayerState state, ClaimHistory history, long today, int cycleLength,
                                   int past, int future) {
        List<Day> days = new ArrayList<>(past + 1 + future);
        for (int offset = -past; offset < 0; offset++) {
            long day = today + offset;
            Integer counted = history.cycleDayOn(day);
            Status status;
            if (counted != null) status = history.unclaimedOn(day) ? Status.AVAILABLE : Status.CLAIMED;
            else if (history.firstDay() == null || day < history.firstDay()) status = Status.BEFORE;
            else status = Status.MISSED;
            days.add(new Day(day, offset, status, counted != null ? counted : 0));
        }

        StreakRules.Displayed shown = StreakRules.display(state, today);
        // The streak today reaches, counted or to count.
        int todayStreak = shown.countedToday() ? shown.streak() : shown.streak() + 1;
        Integer countedToday = history.cycleDayOn(today);
        int todayCycleDay = shown.countedToday() && countedToday != null
                ? countedToday
                : StreakRules.cycleDay(todayStreak, cycleLength);
        boolean claimedToday = shown.countedToday() && !history.unclaimedOn(today);
        days.add(new Day(today, 0, claimedToday ? Status.CLAIMED : Status.AVAILABLE, todayCycleDay));

        for (int offset = 1; offset <= future; offset++) {
            days.add(new Day(today + offset, offset, Status.UPCOMING,
                    StreakRules.cycleDay(todayStreak + offset, cycleLength)));
        }
        return days;
    }
}
