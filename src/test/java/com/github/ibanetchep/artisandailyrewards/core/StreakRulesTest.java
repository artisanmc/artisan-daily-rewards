package com.github.ibanetchep.artisandailyrewards.core;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreakRulesTest {

    private static final CalendarSettings SETTINGS = CalendarSettings.DEFAULTS; // cycle 28, joker every 7, max 2
    private static final long MONDAY = 20_000;

    private final PlayerState fresh = PlayerState.fresh(UUID.randomUUID());

    private static PlayerState claimOn(PlayerState state, long day) {
        StreakRules.Claim claim = StreakRules.claim(state, day, SETTINGS);
        assertTrue(claim.claimed(), "day " + day + " should be claimable");
        return claim.state();
    }

    @Test
    void theFirstClaimStartsAStreakOfOne() {
        StreakRules.Claim claim = StreakRules.claim(fresh, MONDAY, SETTINGS);
        assertEquals(StreakRules.Outcome.FIRST, claim.outcome());
        assertEquals(1, claim.state().streak());
        assertEquals(1, claim.state().longest());
        assertEquals(1, claim.state().totalClaims());
        assertEquals(MONDAY, claim.state().lastDay());
        assertEquals(0, claim.state().brokenStreak());
        assertNull(claim.state().brokenFromDay());
    }

    @Test
    void theNextDayContinuesTheStreak() {
        PlayerState state = claimOn(fresh, MONDAY);
        StreakRules.Claim claim = StreakRules.claim(state, MONDAY + 1, SETTINGS);
        assertEquals(StreakRules.Outcome.CONTINUED, claim.outcome());
        assertEquals(2, claim.state().streak());
        assertEquals(2, claim.state().totalClaims());
    }

    @Test
    void aSecondClaimTheSameDayIsRefusedAndChangesNothing() {
        PlayerState state = claimOn(fresh, MONDAY);
        StreakRules.Claim claim = StreakRules.claim(state, MONDAY, SETTINGS);
        assertFalse(claim.claimed());
        assertEquals(StreakRules.Outcome.ALREADY_CLAIMED, claim.outcome());
        assertEquals(state, claim.state());
    }

    @Test
    void aMissedDayWithoutJokerBreaksTheStreakAndKeepsItForARestore() {
        PlayerState state = fresh;
        for (long day = MONDAY; day < MONDAY + 5; day++) state = claimOn(state, day);
        assertEquals(5, state.streak());
        StreakRules.Claim claim = StreakRules.claim(state, MONDAY + 6, SETTINGS); // day MONDAY+5 missed
        assertEquals(StreakRules.Outcome.BROKEN, claim.outcome());
        assertEquals(1, claim.state().streak());
        assertEquals(5, claim.state().longest());
        assertEquals(5, claim.state().brokenStreak());
        assertEquals(MONDAY + 5, claim.state().brokenFromDay());
        assertEquals(0, claim.freezesUsed());
    }

    @Test
    void jokersCoverMissedDaysOnlyWhenThereAreEnough() {
        PlayerState withTwo = claimOn(fresh, MONDAY).withFreezes(2);

        StreakRules.Claim covered = StreakRules.claim(withTwo, MONDAY + 3, SETTINGS); // 2 days missed
        assertEquals(StreakRules.Outcome.COVERED, covered.outcome());
        assertEquals(2, covered.state().streak(), "covered days do not count");
        assertEquals(0, covered.state().freezes());
        assertEquals(2, covered.freezesUsed());

        StreakRules.Claim broken = StreakRules.claim(withTwo, MONDAY + 4, SETTINGS); // 3 days missed
        assertEquals(StreakRules.Outcome.BROKEN, broken.outcome());
        assertEquals(2, broken.state().freezes(), "no joker is spent on a streak that breaks anyway");
        assertEquals(0, broken.freezesUsed());
    }

    @Test
    void aJokerIsEarnedEverySevenDaysUpToTheStock() {
        PlayerState state = fresh;
        for (long day = MONDAY; day < MONDAY + 6; day++) state = claimOn(state, day);
        assertEquals(0, state.freezes());
        StreakRules.Claim seventh = StreakRules.claim(state, MONDAY + 6, SETTINGS);
        assertTrue(seventh.freezeEarned());
        assertEquals(1, seventh.state().freezes());

        PlayerState full = seventh.state().withFreezes(2);
        for (long day = MONDAY + 7; day < MONDAY + 13; day++) full = claimOn(full, day);
        StreakRules.Claim fourteenth = StreakRules.claim(full, MONDAY + 13, SETTINGS);
        assertEquals(14, fourteenth.state().streak());
        assertFalse(fourteenth.freezeEarned());
        assertEquals(2, fourteenth.state().freezes());
    }

    @Test
    void theCycleLoopsWithoutResettingTheStreak() {
        assertEquals(1, StreakRules.cycleDay(1, 28));
        assertEquals(28, StreakRules.cycleDay(28, 28));
        assertEquals(1, StreakRules.cycleDay(29, 28));
        assertEquals(1, StreakRules.cycle(28, 28));
        assertEquals(2, StreakRules.cycle(29, 28));
        assertEquals(3, StreakRules.cycleDay(10, 7));
    }

    @Test
    void theDisplayedStreakFollowsTheDocumentedWeek() {
        // Cycle of 7 days, 1 joker: the example of the design document.
        CalendarSettings weekly = new CalendarSettings(SETTINGS.resetTime(), 7, SETTINGS.autoClaim(), 0, true, 7, 2, Map.of(), null);
        PlayerState state = new PlayerState(fresh.uuid(), 5, 5, MONDAY - 1, 5, 1, 0, 0, 0, null, null, 0);

        state = StreakRules.claim(state, MONDAY, weekly).state();                 // Monday
        assertEquals(6, state.streak());
        StreakRules.Claim tuesday = StreakRules.claim(state, MONDAY + 1, weekly);   // Tuesday
        state = tuesday.state();
        assertEquals(7, state.streak());
        assertEquals(7, StreakRules.cycleDay(state.streak(), 7));
        assertEquals(2, state.freezes());

        StreakRules.Displayed wednesday = StreakRules.display(state, MONDAY + 2);   // absent
        assertEquals(7, wednesday.streak());
        assertFalse(wednesday.atRisk());
        StreakRules.Displayed thursdayMorning = StreakRules.display(state, MONDAY + 3);
        assertEquals(7, thursdayMorning.streak());
        assertTrue(thursdayMorning.atRisk());

        state = StreakRules.claim(state, MONDAY + 3, weekly).state();             // Thursday
        assertEquals(8, state.streak());
        assertEquals(1, StreakRules.cycleDay(state.streak(), 7));
        assertEquals(1, state.freezes());

        assertTrue(StreakRules.display(state, MONDAY + 5).atRisk());              // Saturday: 1 day missed, 1 joker
        StreakRules.Displayed sunday = StreakRules.display(state, MONDAY + 6);      // 2 days missed
        assertEquals(0, sunday.streak());
        assertTrue(sunday.broken());

        StreakRules.Claim nextMonday = StreakRules.claim(state, MONDAY + 7, weekly);
        assertEquals(1, nextMonday.state().streak());
        assertEquals(8, nextMonday.state().brokenStreak());
        assertEquals(1, nextMonday.state().freezes());
    }

    @Test
    void theDisplayKnowsAClaimMadeToday() {
        PlayerState state = claimOn(fresh, MONDAY);
        StreakRules.Displayed today = StreakRules.display(state, MONDAY);
        assertTrue(today.claimedToday());
        assertEquals(1, today.streak());
        assertEquals(0, StreakRules.display(fresh, MONDAY).streak());
    }
}
