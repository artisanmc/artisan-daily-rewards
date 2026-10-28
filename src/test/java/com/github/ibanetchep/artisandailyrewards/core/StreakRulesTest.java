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

    private static PlayerState countOn(PlayerState state, long day) {
        StreakRules.CheckIn counted = StreakRules.checkIn(state, day, SETTINGS);
        assertTrue(counted.counted(), "day " + day + " should count");
        return counted.state();
    }

    @Test
    void theFirstClaimStartsAStreakOfOne() {
        StreakRules.CheckIn day = StreakRules.checkIn(fresh, MONDAY, SETTINGS);
        assertEquals(StreakRules.Outcome.FIRST, day.outcome());
        assertEquals(1, day.state().streak());
        assertEquals(1, day.state().longest());
        assertEquals(1, day.state().totalDays());
        assertEquals(MONDAY, day.state().lastDay());
        assertEquals(0, day.state().brokenStreak());
        assertNull(day.state().brokenFromDay());
    }

    @Test
    void theNextDayContinuesTheStreak() {
        PlayerState state = countOn(fresh, MONDAY);
        StreakRules.CheckIn day = StreakRules.checkIn(state, MONDAY + 1, SETTINGS);
        assertEquals(StreakRules.Outcome.CONTINUED, day.outcome());
        assertEquals(2, day.state().streak());
        assertEquals(2, day.state().totalDays());
    }

    @Test
    void aSecondClaimTheSameDayIsRefusedAndChangesNothing() {
        PlayerState state = countOn(fresh, MONDAY);
        StreakRules.CheckIn day = StreakRules.checkIn(state, MONDAY, SETTINGS);
        assertFalse(day.counted());
        assertEquals(StreakRules.Outcome.ALREADY_COUNTED, day.outcome());
        assertEquals(state, day.state());
    }

    @Test
    void aMissedDayWithoutJokerBreaksTheStreakAndKeepsItForARestore() {
        PlayerState state = fresh;
        for (long day = MONDAY; day < MONDAY + 5; day++) state = countOn(state, day);
        assertEquals(5, state.streak());
        StreakRules.CheckIn day = StreakRules.checkIn(state, MONDAY + 6, SETTINGS); // day MONDAY+5 missed
        assertEquals(StreakRules.Outcome.BROKEN, day.outcome());
        assertEquals(1, day.state().streak());
        assertEquals(5, day.state().longest());
        assertEquals(5, day.state().brokenStreak());
        assertEquals(MONDAY + 5, day.state().brokenFromDay());
        assertEquals(0, day.freezesUsed());
    }

    @Test
    void jokersCoverMissedDaysOnlyWhenThereAreEnough() {
        PlayerState withTwo = countOn(fresh, MONDAY).withFreezes(2);

        StreakRules.CheckIn covered = StreakRules.checkIn(withTwo, MONDAY + 3, SETTINGS); // 2 days missed
        assertEquals(StreakRules.Outcome.COVERED, covered.outcome());
        assertEquals(2, covered.state().streak(), "covered days do not count");
        assertEquals(0, covered.state().freezes());
        assertEquals(2, covered.freezesUsed());

        StreakRules.CheckIn broken = StreakRules.checkIn(withTwo, MONDAY + 4, SETTINGS); // 3 days missed
        assertEquals(StreakRules.Outcome.BROKEN, broken.outcome());
        assertEquals(2, broken.state().freezes(), "no joker is spent on a streak that breaks anyway");
        assertEquals(0, broken.freezesUsed());
    }

    @Test
    void aJokerIsEarnedEverySevenDaysUpToTheStock() {
        PlayerState state = fresh;
        for (long day = MONDAY; day < MONDAY + 6; day++) state = countOn(state, day);
        assertEquals(0, state.freezes());
        StreakRules.CheckIn seventh = StreakRules.checkIn(state, MONDAY + 6, SETTINGS);
        assertTrue(seventh.freezeEarned());
        assertEquals(1, seventh.state().freezes());

        PlayerState full = seventh.state().withFreezes(2);
        for (long day = MONDAY + 7; day < MONDAY + 13; day++) full = countOn(full, day);
        StreakRules.CheckIn fourteenth = StreakRules.checkIn(full, MONDAY + 13, SETTINGS);
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

        state = StreakRules.checkIn(state, MONDAY, weekly).state();                 // Monday
        assertEquals(6, state.streak());
        StreakRules.CheckIn tuesday = StreakRules.checkIn(state, MONDAY + 1, weekly);   // Tuesday
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

        state = StreakRules.checkIn(state, MONDAY + 3, weekly).state();             // Thursday
        assertEquals(8, state.streak());
        assertEquals(1, StreakRules.cycleDay(state.streak(), 7));
        assertEquals(1, state.freezes());

        assertTrue(StreakRules.display(state, MONDAY + 5).atRisk());              // Saturday: 1 day missed, 1 joker
        StreakRules.Displayed sunday = StreakRules.display(state, MONDAY + 6);      // 2 days missed
        assertEquals(0, sunday.streak());
        assertTrue(sunday.broken());

        StreakRules.CheckIn nextMonday = StreakRules.checkIn(state, MONDAY + 7, weekly);
        assertEquals(1, nextMonday.state().streak());
        assertEquals(8, nextMonday.state().brokenStreak());
        assertEquals(1, nextMonday.state().freezes());
    }

    @Test
    void theDisplayKnowsAClaimMadeToday() {
        PlayerState state = countOn(fresh, MONDAY);
        StreakRules.Displayed today = StreakRules.display(state, MONDAY);
        assertTrue(today.countedToday());
        assertEquals(1, today.streak());
        assertEquals(0, StreakRules.display(fresh, MONDAY).streak());
    }
}
