package com.github.ibanetchep.artisandailyrewards.core;

/**
 * The streak rule, pure: no I/O, no clock. The caller passes the game day ({@link GameClock}) and
 * stores what comes back; the database guarantees the rest (one claim per player and day).
 */
public final class StreakRules {

    private StreakRules() {
    }

    /**
     * The outcome of one claim on {@code today}, and the state to store when it is
     * {@link Claim#claimed()}.
     *
     * @param freezesUsed  jokers spent on the missed days
     * @param freezeEarned whether this claim earned a joker
     */
    public record Claim(PlayerState state, Outcome outcome, int freezesUsed, boolean freezeEarned) {
        public boolean claimed() {
            return outcome != Outcome.ALREADY_CLAIMED;
        }
    }

    public enum Outcome {
        /** First claim ever. */
        FIRST,
        /** The day after the previous claim. */
        CONTINUED,
        /** Days were missed, and jokers covered all of them. */
        COVERED,
        /** Days were missed and the jokers did not cover them: the streak starts again at 1. */
        BROKEN,
        /** Already claimed on this game day. */
        ALREADY_CLAIMED
    }

    /**
     * Claims on {@code today}.
     * <ul>
     *   <li>the day after the last claim: streak + 1;</li>
     *   <li>days missed, all covered by jokers: those jokers are used, streak + 1 (the covered days
     *       do not count);</li>
     *   <li>otherwise the streak breaks: it is kept aside for a restore, from the first missed
     *       day, and starts again at 1. No joker is used.</li>
     * </ul>
     * Then the longest streak, the total, and one joker every {@code freeze_earn_every} days of
     * streak, up to {@code freeze_max_stock}.
     */
    public static Claim claim(PlayerState state, long today, CalendarSettings settings) {
        if (state.claimedOn(today)) return new Claim(state, Outcome.ALREADY_CLAIMED, 0, false);

        int streak;
        int freezes = state.freezes();
        int milestoneMark = state.milestoneMark();
        int brokenStreak = state.brokenStreak();
        int brokenMark = state.brokenMark();
        Long brokenFromDay = state.brokenFromDay();
        int freezesUsed = 0;
        Outcome outcome;

        Long last = state.lastDay();
        if (last == null) {
            streak = 1;
            outcome = Outcome.FIRST;
        } else {
            long missed = today - last - 1;
            if (missed == 0) {
                streak = state.streak() + 1;
                outcome = Outcome.CONTINUED;
            } else if (freezes >= missed) {
                freezesUsed = (int) missed;
                freezes -= freezesUsed;
                streak = state.streak() + 1;
                outcome = Outcome.COVERED;
            } else {
                brokenStreak = state.streak();
                brokenMark = milestoneMark;
                brokenFromDay = last + 1;
                milestoneMark = 0;
                streak = 1;
                outcome = Outcome.BROKEN;
            }
        }

        int earnEvery = settings.freezeEarnEvery();
        boolean earned = earnEvery > 0 && streak % earnEvery == 0 && freezes < settings.freezeMaxStock();
        if (earned) freezes++;

        PlayerState next = new PlayerState(state.uuid(), streak, Math.max(state.longest(), streak), today,
                state.totalClaims() + 1, freezes, milestoneMark, brokenStreak, brokenMark, brokenFromDay,
                state.autoClaim(), state.version());
        return new Claim(next, outcome, freezesUsed, earned);
    }

    /** What a player sees on {@code today}: the stored streak, unless it is already lost. */
    public record Displayed(int streak, boolean claimedToday, boolean atRisk, boolean broken) {
    }

    /**
     * The streak to show: the stored one while the last claim was today or yesterday; still the
     * stored one, {@code atRisk}, while the jokers cover the days missed (they are used at the next
     * claim); 0 and {@code broken} once they cannot.
     */
    public static Displayed display(PlayerState state, long today) {
        Long last = state.lastDay();
        if (last == null) return new Displayed(0, false, false, false);
        if (last >= today) return new Displayed(state.streak(), true, false, false);
        long missed = today - last - 1;
        if (missed == 0) return new Displayed(state.streak(), false, false, false);
        if (state.freezes() >= missed) return new Displayed(state.streak(), false, true, false);
        return new Displayed(0, false, false, true);
    }

    /** Day of the cycle for a streak, 1 to {@code cycleLength}: day 29 of a 28-day cycle is day 1. */
    public static int cycleDay(int streak, int cycleLength) {
        if (streak < 1) return 1;
        return ((streak - 1) % cycleLength) + 1;
    }

    /** Number of the cycle a streak is in, from 1. */
    public static int cycle(int streak, int cycleLength) {
        if (streak < 1) return 1;
        return (streak - 1) / cycleLength + 1;
    }
}
