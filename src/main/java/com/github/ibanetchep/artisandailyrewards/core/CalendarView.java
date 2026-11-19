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
     * @param offset    days from today: negative in the past, 0 today
     * @param cycleDay  the day of the cycle paid (claimed) or to be paid (available, upcoming); 0
     *                  for a day missed or before the first one
     * @param streak    the streak that day reaches (today and to come: if the player comes every
     *                  day); for a day before today, the streak it reached when it paid milestones,
     *                  else 0
     * @param milestone the day pays milestones on top of its reward (paid, waiting, or to come if
     *                  the player comes every day)
     */
    public record Day(long day, int offset, Status status, int cycleDay, int streak, boolean milestone) {
        public Day(long day, int offset, Status status, int cycleDay) {
            this(day, offset, status, cycleDay, 0, false);
        }
    }

    /**
     * Days to add before today so that the first day shown sits on the first column of a grid
     * {@code week} days wide, as the Rewards grid lays the cycle out: today goes on the column
     * of its day of the cycle, and days 7, 14, 21… of a cycle on the last one. 0 for {@code week}
     * below 2.
     */
    public static int weekShift(PlayerState state, ClaimHistory history, long today, int cycleLength, int week) {
        if (week < 2) return 0;
        return (todayCycleDay(state, history, today, cycleLength) - 1) % week;
    }

    /** Today's day of the cycle, counted or to count, as {@link #around} shows it. */
    static int todayCycleDay(PlayerState state, ClaimHistory history, long today, int cycleLength) {
        StreakRules.Displayed shown = StreakRules.display(state, today);
        int todayStreak = shown.countedToday() ? shown.streak() : shown.streak() + 1;
        Integer countedToday = history.cycleDayOn(today);
        return shown.countedToday() && countedToday != null
                ? countedToday
                : StreakRules.cycleDay(todayStreak, cycleLength);
    }

    /**
     * {@code past} days before today, today, then {@code future} days after it. The past comes
     * from {@code history}: a day it does not hold is missed, unless it is before the first one.
     * Today and the future follow the streak as {@link StreakRules#display} shows it: a broken
     * streak starts again at day 1, one the jokers still cover goes on.
     */
    public static List<Day> around(PlayerState state, ClaimHistory history, long today, int cycleLength,
                                   int past, int future) {
        return around(state, history, today, cycleLength, List.of(), past, future);
    }

    /**
     * As {@link #around(PlayerState, ClaimHistory, long, int, int, int)}, with the days that pay
     * {@code milestones}: before today as they were counted, today and after as
     * {@link StreakRules#checkIn} will decide if the player comes every day (a streak above the
     * milestone mark that a milestone matches; the mark is 0 once the streak is broken).
     */
    public static List<Day> around(PlayerState state, ClaimHistory history, long today, int cycleLength,
                                   List<Milestone> milestones, int past, int future) {
        List<Day> days = new ArrayList<>(past + 1 + future);
        for (int offset = -past; offset < 0; offset++) {
            long day = today + offset;
            Integer counted = history.cycleDayOn(day);
            Status status;
            if (counted != null) status = history.unclaimedOn(day) ? Status.AVAILABLE : Status.CLAIMED;
            else if (history.firstDay() == null || day < history.firstDay()) status = Status.BEFORE;
            else status = Status.MISSED;
            Integer milestoneStreak = history.milestoneOn(day);
            days.add(new Day(day, offset, status, counted != null ? counted : 0,
                    milestoneStreak != null ? milestoneStreak : 0, milestoneStreak != null));
        }

        StreakRules.Displayed shown = StreakRules.display(state, today);
        // The streak today reaches, counted or to count.
        int todayStreak = shown.countedToday() ? shown.streak() : shown.streak() + 1;
        int todayCycleDay = todayCycleDay(state, history, today, cycleLength);
        boolean claimedToday = shown.countedToday() && !history.unclaimedOn(today);
        // The mark today's streak is checked against: the stored one, unless the streak is broken.
        int mark = shown.broken() ? 0 : state.milestoneMark();
        boolean todayMilestone = shown.countedToday()
                ? history.milestoneOn(today) != null
                : todayStreak > mark && Milestone.paidAt(milestones, todayStreak).size() > 0;
        days.add(new Day(today, 0, claimedToday ? Status.CLAIMED : Status.AVAILABLE, todayCycleDay,
                todayStreak, todayMilestone));
        if (todayMilestone) mark = Math.max(mark, todayStreak);

        for (int offset = 1; offset <= future; offset++) {
            int streak = todayStreak + offset;
            boolean milestone = streak > mark && !Milestone.paidAt(milestones, streak).isEmpty();
            days.add(new Day(today + offset, offset, Status.UPCOMING,
                    StreakRules.cycleDay(streak, cycleLength), streak, milestone));
        }
        return days;
    }
}
