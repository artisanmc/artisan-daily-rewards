package com.github.ibanetchep.artisandailyrewards.storage;

import com.github.ibanetchep.artisandailyrewards.core.PlayerState;
import com.github.ibanetchep.artisandailyrewards.core.StreakRules;
import org.jetbrains.annotations.Nullable;

/**
 * What counting a day did. {@link #state} is the player's row after it (before it when nothing
 * was written).
 *
 * @param outcome     null when nothing was counted: see {@link #status}
 * @param freezesUsed jokers spent on missed days
 * @param cycleDay    day of the cycle this day pays when its reward is claimed
 */
public record CheckInResult(
        Status status,
        PlayerState before,
        PlayerState state,
        @Nullable StreakRules.Outcome outcome,
        int freezesUsed,
        boolean freezeEarned,
        int cycleDay
) {

    public enum Status {
        COUNTED,
        /** This game day already counts, here or on another server. */
        ALREADY_COUNTED,
        /** Another server kept writing this player; nothing was counted, it is tried again later. */
        CONFLICT
    }

    static CheckInResult counted(PlayerState before, PlayerState after, StreakRules.Outcome outcome,
                                 int freezesUsed, boolean freezeEarned, int cycleDay) {
        return new CheckInResult(Status.COUNTED, before, after, outcome, freezesUsed, freezeEarned, cycleDay);
    }

    static CheckInResult alreadyCounted(PlayerState state) {
        return new CheckInResult(Status.ALREADY_COUNTED, state, state, null, 0, false, 0);
    }

    static CheckInResult conflict(PlayerState state) {
        return new CheckInResult(Status.CONFLICT, state, state, null, 0, false, 0);
    }

    public boolean counted() {
        return status == Status.COUNTED;
    }
}
