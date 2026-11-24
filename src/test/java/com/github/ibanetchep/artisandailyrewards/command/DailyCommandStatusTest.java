package com.github.ibanetchep.artisandailyrewards.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DailyCommandStatusTest {

    @Test
    void todayWaitingForItsPlaytimeOffersNoClaim() {
        assertNull(DailyCommand.claimKey(0, false, true));
    }

    @Test
    void todayCountableOffersTheClaim() {
        assertEquals("status.waiting", DailyCommand.claimKey(0, false, false));
    }

    @Test
    void aPastRewardStillWaitsWhileTodayIsLocked() {
        assertEquals("status.waiting", DailyCommand.claimKey(1, false, true));
        assertEquals("status.waiting_many", DailyCommand.claimKey(2, false, true));
    }

    @Test
    void todayCountedAndClaimed() {
        assertEquals("status.claimed", DailyCommand.claimKey(0, true, false));
    }

    @Test
    void todayCountedNotClaimed() {
        assertEquals("status.waiting", DailyCommand.claimKey(1, true, false));
    }

    @Test
    void autoclaimTakesOnOffAndTrueFalse() {
        for (String on : new String[]{"on", "true", "ON", "True", " yes ", "oui"}) {
            assertEquals(Boolean.TRUE, DailyCommand.parseToggle(on), on);
        }
        for (String off : new String[]{"off", "false", "OFF", "no", "non"}) {
            assertEquals(Boolean.FALSE, DailyCommand.parseToggle(off), off);
        }
        assertNull(DailyCommand.parseToggle("maybe"));
    }
}
