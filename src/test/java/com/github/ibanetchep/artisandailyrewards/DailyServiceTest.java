package com.github.ibanetchep.artisandailyrewards;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class DailyServiceTest {

    @Test
    void aJoinBeforeThePlaytimeNamesOnlyTheDaysBeforeThatWait() {
        assertEquals(0, DailyService.remindedWhileLocked(0, false, true), "only today, not unlocked yet: no reminder");
        assertEquals(2, DailyService.remindedWhileLocked(2, false, true), "two days before wait, today is not one");
        assertEquals(0, DailyService.remindedWhileLocked(2, true, true), "auto-claim: claimed for the player, no reminder");
        assertEquals(0, DailyService.remindedWhileLocked(2, false, false), "notify_on_unlock: false");
    }

    @Test
    void anOpenCalendarIsRefreshedWhenTheMinutesLeftChange() {
        assertEquals(Duration.ofSeconds(30), DailyService.untilMinuteTurns(Duration.ofSeconds(4 * 60 + 30)), "4 min until 4:00");
        assertEquals(Duration.ofMinutes(1), DailyService.untilMinuteTurns(Duration.ofMinutes(5)), "5 min until 4:00");
        assertEquals(Duration.ofSeconds(59), DailyService.untilMinuteTurns(Duration.ofSeconds(59)), "< 1 min until unlocked");
    }
}
