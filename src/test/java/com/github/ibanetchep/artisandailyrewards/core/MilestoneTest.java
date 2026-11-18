package com.github.ibanetchep.artisandailyrewards.core;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MilestoneTest {

    private static final Milestone WEEK = Milestone.at(7, "week", List.of("w"));
    private static final Milestone MONTHLY = Milestone.every(30, 30, "month", List.of("m"));
    private static final Milestone TENS_FROM_50 = Milestone.every(10, 50, null, null);

    private static CalendarSettings with(List<Milestone> milestones) {
        CalendarSettings d = CalendarSettings.DEFAULTS;
        return new CalendarSettings(LocalTime.MIDNIGHT, d.cycleLength(), d.autoClaim(), 0, true, 0, 0,
                Map.of(), null, milestones);
    }

    @Test
    void atMatchesOnceAndEveryFromItsStart() {
        assertTrue(WEEK.matches(7));
        assertFalse(WEEK.matches(14));
        assertFalse(MONTHLY.matches(15));
        assertTrue(MONTHLY.matches(30));
        assertTrue(MONTHLY.matches(90));
        assertFalse(TENS_FROM_50.matches(40));
        assertTrue(TENS_FROM_50.matches(50));
        assertTrue(TENS_FROM_50.matches(70));
        assertFalse(TENS_FROM_50.matches(75));
        assertFalse(MONTHLY.matches(0));
    }

    @Test
    void nextFromFindsTheNextStreak() {
        assertEquals(7, WEEK.nextFrom(1));
        assertEquals(7, WEEK.nextFrom(7));
        assertEquals(-1, WEEK.nextFrom(8));
        assertEquals(30, MONTHLY.nextFrom(1));
        assertEquals(60, MONTHLY.nextFrom(31));
        assertEquals(60, MONTHLY.nextFrom(60));
        assertEquals(50, TENS_FROM_50.nextFrom(3));
        assertEquals(60, TENS_FROM_50.nextFrom(51));
    }

    @Test
    void upcomingMergesByStreakAndKeepsTheFileOrderOnATie() {
        List<Milestone> all = List.of(MONTHLY, WEEK, TENS_FROM_50);
        List<Milestone.Occurrence> next = Milestone.upcoming(all, 1, 6);
        assertEquals(List.of(7, 30, 50, 60, 60, 70), next.stream().map(Milestone.Occurrence::streak).toList());
        assertEquals(MONTHLY, next.get(3).milestone());
        assertEquals(TENS_FROM_50, next.get(4).milestone());
        // One-time milestones run out.
        assertEquals(List.of(7), Milestone.upcoming(List.of(WEEK), 1, 5).stream().map(Milestone.Occurrence::streak).toList());
        assertTrue(Milestone.upcoming(List.of(WEEK), 8, 5).isEmpty());
    }

    @Test
    void readsAtEveryAndFrom() throws CalendarException {
        CalendarSettings read = CalendarSettings.read(Map.of("milestones", List.of(
                Map.of("at", 7, "icon", Map.of("material", "DIAMOND"), "actions", List.of()),
                Map.of("every", 30),
                Map.of("every", "10", "from", 50))));
        assertEquals(List.of(Milestone.at(7, Map.of("material", "DIAMOND"), List.of()),
                Milestone.every(30, 30, null, null), Milestone.every(10, 50, null, null)), read.milestones());
        assertEquals(List.of(), CalendarSettings.read(Map.of()).milestones());
    }

    @Test
    void refusesAWrongMilestone() {
        assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of("milestones", Map.of("at", 7))));
        assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of("milestones", List.of(Map.of("icon", "x")))));
        assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of("milestones", List.of(Map.of("at", 7, "every", 7)))));
        assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of("milestones", List.of(Map.of("at", 7, "from", 7)))));
        assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of("milestones", List.of(Map.of("every", 0)))));
        assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of("milestones", List.of(Map.of("at", "x")))));
    }

    @Test
    void aCheckInPaysTheMilestonesOncePerStreak() {
        CalendarSettings settings = with(List.of(WEEK, Milestone.every(3, 3, null, null)));
        PlayerState state = PlayerState.fresh(UUID.randomUUID());
        List<Integer> paid = new java.util.ArrayList<>();
        long day = 20_000;
        for (int i = 1; i <= 9; i++) {
            StreakRules.CheckIn c = StreakRules.checkIn(state, day++, settings);
            if (c.milestone()) paid.add(c.state().streak());
            state = c.state();
        }
        assertEquals(List.of(3, 6, 7, 9), paid);
        assertEquals(9, state.milestoneMark());

        // A break: the mark starts again, the milestones are paid again in the new streak.
        day += 3;
        StreakRules.CheckIn broken = StreakRules.checkIn(state, day++, settings);
        assertEquals(StreakRules.Outcome.BROKEN, broken.outcome());
        assertEquals(0, broken.state().milestoneMark());
        assertEquals(9, broken.state().brokenMark());
        state = broken.state();
        state = StreakRules.checkIn(state, day++, settings).state();
        StreakRules.CheckIn third = StreakRules.checkIn(state, day, settings);
        assertTrue(third.milestone());
        assertEquals(3, third.state().milestoneMark());
    }

    @Test
    void aStreakAtOrBelowTheMarkPaysNothing() {
        CalendarSettings settings = with(List.of(Milestone.every(1, 1, null, null)));
        // The mark is above the streak (an admin lowered it): the steps already paid are not paid again.
        PlayerState state = new PlayerState(UUID.randomUUID(), 4, 10, 19_999L, 10, 0, 10, 0, 0, null, null, 1);
        StreakRules.CheckIn c = StreakRules.checkIn(state, 20_000, settings);
        assertEquals(5, c.state().streak());
        assertFalse(c.milestone());
        assertEquals(10, c.state().milestoneMark());
    }
}
