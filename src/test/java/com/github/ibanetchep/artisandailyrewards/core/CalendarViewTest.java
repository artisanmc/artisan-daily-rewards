package com.github.ibanetchep.artisandailyrewards.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalendarViewTest {

    private static final long TODAY = 20_000;
    private static final UUID ID = UUID.randomUUID();

    private static PlayerState state(int streak, Long lastDay, int freezes) {
        return new PlayerState(ID, streak, streak, lastDay, streak, freezes, 0, 0, 0, null, null, 1);
    }

    private static List<CalendarView.Status> statuses(List<CalendarView.Day> days) {
        return days.stream().map(CalendarView.Day::status).toList();
    }

    private static List<Integer> cycleDays(List<CalendarView.Day> days) {
        return days.stream().map(CalendarView.Day::cycleDay).toList();
    }

    @Test
    void aNewPlayerHasNothingBehindAndStartsAtDayOne() {
        List<CalendarView.Day> days = CalendarView.around(PlayerState.fresh(ID), ClaimHistory.EMPTY, TODAY, 7, 2, 3);
        assertEquals(List.of(CalendarView.Status.BEFORE, CalendarView.Status.BEFORE, CalendarView.Status.AVAILABLE,
                CalendarView.Status.UPCOMING, CalendarView.Status.UPCOMING, CalendarView.Status.UPCOMING), statuses(days));
        assertEquals(List.of(0, 0, 1, 2, 3, 4), cycleDays(days));
        assertEquals(List.of(-2, -1, 0, 1, 2, 3), days.stream().map(CalendarView.Day::offset).toList());
        assertEquals(TODAY - 2, days.get(0).day());
    }

    @Test
    void thePastIsTheClaimsAsTheyWentAndTheFutureLoopsTheCycle() {
        // Claimed the day before yesterday (day 5) and yesterday (day 6), not today yet.
        ClaimHistory history = ClaimHistory.of(TODAY - 10, Map.of(TODAY - 2, 5, TODAY - 1, 6));
        List<CalendarView.Day> days = CalendarView.around(state(6, TODAY - 1, 0), history, TODAY, 7, 3, 3);
        assertEquals(List.of(CalendarView.Status.MISSED, CalendarView.Status.CLAIMED, CalendarView.Status.CLAIMED,
                CalendarView.Status.AVAILABLE, CalendarView.Status.UPCOMING, CalendarView.Status.UPCOMING,
                CalendarView.Status.UPCOMING), statuses(days));
        assertEquals(List.of(0, 5, 6, 7, 1, 2, 3), cycleDays(days));
    }

    @Test
    void todayClaimedShowsTheDayItPaid() {
        ClaimHistory history = ClaimHistory.of(TODAY, Map.of(TODAY, 3));
        List<CalendarView.Day> days = CalendarView.around(state(3, TODAY, 0), history, TODAY, 7, 0, 2);
        assertEquals(List.of(CalendarView.Status.CLAIMED, CalendarView.Status.UPCOMING, CalendarView.Status.UPCOMING),
                statuses(days));
        assertEquals(List.of(3, 4, 5), cycleDays(days));
    }

    @Test
    void aBrokenStreakStartsAgainAtDayOne() {
        // Last claim three days ago, no joker: the streak of 10 is lost.
        ClaimHistory history = ClaimHistory.of(TODAY - 20, Map.of(TODAY - 3, 10));
        List<CalendarView.Day> days = CalendarView.around(state(10, TODAY - 3, 0), history, TODAY, 28, 3, 1);
        assertEquals(List.of(CalendarView.Status.CLAIMED, CalendarView.Status.MISSED, CalendarView.Status.MISSED,
                CalendarView.Status.AVAILABLE, CalendarView.Status.UPCOMING), statuses(days));
        assertEquals(List.of(10, 0, 0, 1, 2), cycleDays(days));
    }

    @Test
    void jokersThatCoverTheGapKeepTheStreakGoing() {
        ClaimHistory history = ClaimHistory.of(TODAY - 20, Map.of(TODAY - 2, 4));
        List<CalendarView.Day> days = CalendarView.around(state(4, TODAY - 2, 1), history, TODAY, 28, 2, 1);
        assertEquals(List.of(CalendarView.Status.CLAIMED, CalendarView.Status.MISSED, CalendarView.Status.AVAILABLE,
                CalendarView.Status.UPCOMING), statuses(days));
        assertEquals(List.of(4, 0, 5, 6), cycleDays(days));
    }

    @Test
    void aClaimIsAddedToTheHistory() {
        ClaimHistory history = ClaimHistory.EMPTY.with(TODAY, 1);
        assertEquals(TODAY, history.firstDay());
        assertEquals(1, history.cycleDayOn(TODAY));
        assertEquals(TODAY - 5, ClaimHistory.of(TODAY - 5, Map.of()).with(TODAY, 2).firstDay());
    }
}
