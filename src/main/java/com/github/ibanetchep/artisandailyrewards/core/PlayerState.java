package com.github.ibanetchep.artisandailyrewards.core;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A player's stored counters, one row of the players table. The stored {@link #streak} can be
 * stale: a break is only written when the player is next there, so anything shown to a player goes through
 * {@link StreakRules#display}.
 *
 * @param lastDay        last game day counted for the streak (the player was there), null before
 *                       the first one
 * @param totalDays      game days counted, ever
 * @param milestoneMark  highest milestone paid in the current streak
 * @param brokenStreak   streak lost at the last break, 0 once restored or expired
 * @param brokenMark     milestone mark lost at the last break
 * @param brokenFromDay  first missed day the jokers did not cover: start of the restore window
 * @param autoClaim      the player's own choice when the calendar says {@code auto_claim: player},
 *                       null when they never chose
 * @param version        optimistic concurrency counter, raised by every write
 */
public record PlayerState(
        UUID uuid,
        int streak,
        int longest,
        @Nullable Long lastDay,
        int totalDays,
        int freezes,
        int milestoneMark,
        int brokenStreak,
        int brokenMark,
        @Nullable Long brokenFromDay,
        @Nullable Boolean autoClaim,
        int version
) {

    /** A player never counted, with no row yet. */
    public static PlayerState fresh(UUID uuid) {
        return new PlayerState(uuid, 0, 0, null, 0, 0, 0, 0, 0, null, null, 0);
    }

    public boolean countedOn(long day) {
        return lastDay != null && lastDay >= day;
    }

    public PlayerState withStreak(int value) {
        return new PlayerState(uuid, value, longest, lastDay, totalDays, freezes, milestoneMark,
                brokenStreak, brokenMark, brokenFromDay, autoClaim, version);
    }

    public PlayerState withLongest(int value) {
        return new PlayerState(uuid, streak, value, lastDay, totalDays, freezes, milestoneMark,
                brokenStreak, brokenMark, brokenFromDay, autoClaim, version);
    }

    public PlayerState withFreezes(int value) {
        return new PlayerState(uuid, streak, longest, lastDay, totalDays, value, milestoneMark,
                brokenStreak, brokenMark, brokenFromDay, autoClaim, version);
    }

    public PlayerState withAutoClaim(@Nullable Boolean value) {
        return new PlayerState(uuid, streak, longest, lastDay, totalDays, freezes, milestoneMark,
                brokenStreak, brokenMark, brokenFromDay, value, version);
    }

    public PlayerState withVersion(int value) {
        return new PlayerState(uuid, streak, longest, lastDay, totalDays, freezes, milestoneMark,
                brokenStreak, brokenMark, brokenFromDay, autoClaim, value);
    }
}
