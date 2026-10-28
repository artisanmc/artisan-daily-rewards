package com.github.ibanetchep.artisandailyrewards.core;

import java.util.ArrayList;
import java.util.List;

/**
 * The calendar a player sees, pure: the recent game days as they went (claimed, missed), today,
 * and the days to come with the day of the cycle each one will pay if the player claims every
 * day from now on.
 */
public final class CalendarView {

    private CalendarView() {
    }

    public enum Status {
        /** Before the player's first claim: not missed, they were not there yet. */
        BEFORE,
        /** Claimed, today or before. */
        CLAIMED,
        /** Not claimed, and over. A joker may have saved the streak; the reward is lost either way. */
        MISSED,
        /** Today, not claimed yet. */
        AVAILABLE,
        /** A day to come. */
        UPCOMING
    }

    /**
     * @param offset   days from today: negative in the past, 0 today
     * @param cycleDay the day of the cycle paid (claimed) or to be paid (available, upcoming); 0
     *                 for a day missed or before the first claim
     */
    public record Day(long day, int offset, Status status, int cycleDay) {
    }

    /**
     * {@code past} days before today, today, then {@code future} days after it. The past comes
     * from {@code history}: a day it does not hold is missed, unless it is before the first claim.
     * Today and the future follow the streak as {@link StreakRules#display} shows it: a broken
     * streak starts again at day 1, one the jokers still cover goes on.
     */
    public static List<Day> around(PlayerState state, ClaimHistory history, long today, int cycleLength,
                                   int past, int future) {
        List<Day> days = new ArrayList<>(past + 1 + future);
        for (int offset = -past; offset < 0; offset++) {
            long day = today + offset;
            Integer claimed = history.cycleDayOn(day);
            Status status;
            if (claimed != null) status = Status.CLAIMED;
            else if (history.firstDay() == null || day < history.firstDay()) status = Status.BEFORE;
            else status = Status.MISSED;
            days.add(new Day(day, offset, status, claimed != null ? claimed : 0));
        }

        StreakRules.Displayed shown = StreakRules.display(state, today);
        // The streak today's claim reaches, made or to make.
        int todayStreak = shown.claimedToday() ? shown.streak() : shown.streak() + 1;
        Integer claimedToday = history.cycleDayOn(today);
        int todayCycleDay = shown.claimedToday() && claimedToday != null
                ? claimedToday
                : StreakRules.cycleDay(todayStreak, cycleLength);
        days.add(new Day(today, 0, shown.claimedToday() ? Status.CLAIMED : Status.AVAILABLE, todayCycleDay));

        for (int offset = 1; offset <= future; offset++) {
            days.add(new Day(today + offset, offset, Status.UPCOMING,
                    StreakRules.cycleDay(todayStreak + offset, cycleLength)));
        }
        return days;
    }
}
