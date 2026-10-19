package com.github.ibanetchep.artisandailyrewards.core;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * The game day: a calendar day in the server's time zone that starts at the reset time, as an
 * epoch day. Calendar days, not rolling 24 hours: a player who claims a little later each day
 * never loses a day. A daylight saving change makes one day 23 or 25 hours long and skips none.
 *
 * <p>{@link #shift} moves this server's day for tests ({@code /dailyadmin day}); it is never
 * stored.
 */
public final class GameClock {

    private final Clock clock;
    private final ZoneId zone;
    private volatile LocalTime resetTime;
    private volatile long offsetDays;

    public GameClock(Clock clock, LocalTime resetTime) {
        this.clock = clock;
        this.zone = clock.getZone();
        this.resetTime = resetTime;
    }

    /** The server's clock and time zone ({@code -Duser.timezone}). */
    public static GameClock system(LocalTime resetTime) {
        return new GameClock(Clock.systemDefaultZone(), resetTime);
    }

    public ZoneId zone() {
        return zone;
    }

    public void setResetTime(LocalTime resetTime) {
        this.resetTime = resetTime;
    }

    public LocalTime resetTime() {
        return resetTime;
    }

    public Instant now() {
        return clock.instant();
    }

    public long today() {
        return dayOf(now()) + offsetDays;
    }

    /** The game day {@code instant} falls in, without the test shift. */
    public long dayOf(Instant instant) {
        ZonedDateTime local = instant.atZone(zone);
        LocalDate date = local.toLocalDate();
        if (local.toLocalTime().isBefore(resetTime)) date = date.minusDays(1);
        return date.toEpochDay();
    }

    /** When the current game day ends (real time, the shift does not move it). */
    public Instant nextReset() {
        Instant now = now();
        LocalDate day = LocalDate.ofEpochDay(dayOf(now));
        // A reset inside a daylight saving gap moves forward to the first valid instant.
        return ZonedDateTime.of(day.plusDays(1), resetTime, zone).toInstant();
    }

    public Duration untilReset() {
        Duration left = Duration.between(now(), nextReset());
        return left.isNegative() ? Duration.ZERO : left;
    }

    public long offsetDays() {
        return offsetDays;
    }

    public void shift(long days) {
        offsetDays += days;
    }

    public void resetShift() {
        offsetDays = 0;
    }
}
