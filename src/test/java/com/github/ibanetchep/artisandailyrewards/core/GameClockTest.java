package com.github.ibanetchep.artisandailyrewards.core;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameClockTest {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    private static GameClock at(String localDateTime, LocalTime reset) {
        Instant instant = LocalDateTime.parse(localDateTime).atZone(PARIS).toInstant();
        return new GameClock(Clock.fixed(instant, PARIS), reset);
    }

    private static long day(String date) {
        return LocalDate.parse(date).toEpochDay();
    }

    @Test
    void aDayIsTheCalendarDayWhenTheResetIsMidnight() {
        assertEquals(day("2026-10-19"), at("2026-10-19T00:00:00", LocalTime.MIDNIGHT).today());
        assertEquals(day("2026-10-19"), at("2026-10-19T23:59:59", LocalTime.MIDNIGHT).today());
    }

    @Test
    void beforeTheResetHourItIsStillTheDayBefore() {
        LocalTime six = LocalTime.of(6, 0);
        assertEquals(day("2026-10-18"), at("2026-10-19T05:59:00", six).today());
        assertEquals(day("2026-10-19"), at("2026-10-19T06:00:00", six).today());
    }

    @Test
    void daylightSavingChangesSkipNoDay() {
        // Paris goes from 03:00 back to 02:00 on 2026-10-25, and from 02:00 to 03:00 on 2026-03-29.
        assertEquals(day("2026-10-25"), at("2026-10-25T12:00:00", LocalTime.MIDNIGHT).today());
        assertEquals(day("2026-10-26"), at("2026-10-26T00:30:00", LocalTime.MIDNIGHT).today());
        assertEquals(day("2026-03-29"), at("2026-03-29T12:00:00", LocalTime.MIDNIGHT).today());
        assertEquals(day("2026-03-30"), at("2026-03-30T00:30:00", LocalTime.MIDNIGHT).today());
    }

    @Test
    void theLongDayLastsTwentyFiveHours() {
        GameClock clock = at("2026-10-25T00:00:00", LocalTime.MIDNIGHT);
        assertEquals(Duration.ofHours(25), clock.untilReset());
        GameClock shortDay = at("2026-03-29T00:00:00", LocalTime.MIDNIGHT);
        assertEquals(Duration.ofHours(23), shortDay.untilReset());
    }

    @Test
    void aResetInsideTheSpringGapMovesForward() {
        // 02:30 does not exist on 2026-03-29 in Paris: that day's reset happens at 03:30.
        GameClock clock = at("2026-03-29T01:00:00", LocalTime.of(2, 30));
        assertEquals(day("2026-03-28"), clock.today());
        assertEquals(LocalDateTime.parse("2026-03-29T03:30:00").atZone(PARIS).toInstant(), clock.nextReset());
    }

    @Test
    void theTestShiftMovesTheDayOnly() {
        GameClock clock = at("2026-10-19T12:00:00", LocalTime.MIDNIGHT);
        clock.shift(2);
        assertEquals(day("2026-10-21"), clock.today());
        assertEquals(Duration.ofHours(12), clock.untilReset());
        clock.resetShift();
        assertEquals(day("2026-10-19"), clock.today());
    }
}
