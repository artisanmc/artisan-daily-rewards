package com.github.ibanetchep.artisandailyrewards.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class DurationsTest {

    @Test
    void aCountdownShowsWholeMinutesThenLessThanOne() {
        assertEquals("5 min", Durations.countdown(Duration.ofMinutes(5)));
        assertEquals("4 min", Durations.countdown(Duration.ofSeconds(4 * 60 + 59)));
        assertEquals("1 min", Durations.countdown(Duration.ofSeconds(60)));
        assertEquals("< 1 min", Durations.countdown(Duration.ofSeconds(59)));
    }
}
