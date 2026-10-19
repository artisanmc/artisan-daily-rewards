package com.github.ibanetchep.artisandailyrewards.storage;

import com.github.ibanetchep.artisandailyrewards.core.PlayerState;
import com.github.ibanetchep.artisandailyrewards.core.StreakRules;
import org.jetbrains.annotations.Nullable;

/**
 * What a claim did. {@link #state} is the player's row after it (before it when nothing was
 * written).
 *
 * @param outcome     null when nothing was claimed: see {@link #status}
 * @param freezesUsed jokers spent on missed days
 * @param cycleDay    day of the cycle this claim paid
 */
public record ClaimResult(
        Status status,
        PlayerState before,
        PlayerState state,
        @Nullable StreakRules.Outcome outcome,
        int freezesUsed,
        boolean freezeEarned,
        int cycleDay
) {

    public enum Status {
        CLAIMED,
        /** Already claimed this game day, here or on another server. */
        ALREADY_CLAIMED,
        /** Another server kept writing this player; nothing was claimed, the player may retry. */
        CONFLICT
    }

    static ClaimResult claimed(PlayerState before, PlayerState after, StreakRules.Outcome outcome,
                               int freezesUsed, boolean freezeEarned, int cycleDay) {
        return new ClaimResult(Status.CLAIMED, before, after, outcome, freezesUsed, freezeEarned, cycleDay);
    }

    static ClaimResult alreadyClaimed(PlayerState state) {
        return new ClaimResult(Status.ALREADY_CLAIMED, state, state, null, 0, false, 0);
    }

    static ClaimResult conflict(PlayerState state) {
        return new ClaimResult(Status.CONFLICT, state, state, null, 0, false, 0);
    }

    public boolean claimed() {
        return status == Status.CLAIMED;
    }
}
