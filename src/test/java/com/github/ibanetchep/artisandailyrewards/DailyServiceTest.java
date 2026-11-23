package com.github.ibanetchep.artisandailyrewards;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DailyServiceTest {

    @Test
    void aJoinBeforeThePlaytimeRemindsOnlyOfTheDaysBeforeThatWait() {
        assertEquals(0, DailyService.waitingWhileLocked(0), "nothing waits: no reminder, today is not unlocked yet");
        assertEquals(2, DailyService.waitingWhileLocked(2), "two days before wait: today is not one of them");
    }
}
