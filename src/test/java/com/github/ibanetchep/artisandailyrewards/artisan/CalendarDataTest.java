package com.github.ibanetchep.artisandailyrewards.artisan;

import com.github.ibanetchep.artisandailyrewards.core.CalendarSettings;
import com.github.ibanetchep.artisandailyrewards.core.CalendarView;
import com.github.ibanetchep.artisandailyrewards.core.Milestone;
import com.github.ibanetchep.artisandailyrewards.DailyService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalendarDataTest {

    private static final long DAY = LocalDate.of(2026, 10, 25).toEpochDay();
    private static final CalendarSettings.DayReward DIAMONDS = new CalendarSettings.DayReward(Map.of("material", "DIAMOND"), null);
    private static final CalendarSettings SETTINGS = new CalendarSettings(CalendarSettings.DEFAULTS.resetTime(), 7,
            CalendarSettings.AutoClaim.OFF, 0, true, 7, 2, Map.of(7, DIAMONDS), null);

    @Test
    void aRowCarriesTheDayItsStatusAndItsReward() {
        Map<String, Object> row = CalendarData.row(new CalendarView.Day(DAY + 1, 1, CalendarView.Status.UPCOMING, 7), SETTINGS);
        assertEquals("2026-10-26", row.get("id"));
        assertEquals("26/10", row.get("date"));
        assertEquals("upcoming", row.get("status"));
        assertEquals(true, row.get("is_upcoming"));
        assertEquals(false, row.get("is_today"));
        assertEquals(7, row.get("cycle_day"));
        assertEquals(true, row.get("has_reward"));
        assertEquals(Map.of("material", "DIAMOND"), row.get("icon"));
    }

    @Test
    void aDayWithoutRewardOrMissedShowsAPlainIcon() {
        Map<String, Object> plain = CalendarData.row(new CalendarView.Day(DAY, 0, CalendarView.Status.AVAILABLE, 3), SETTINGS);
        assertEquals(false, plain.get("has_reward"));
        assertEquals(CalendarData.NO_REWARD_ICON, plain.get("icon"));
        assertEquals(true, plain.get("is_today"));
        assertEquals(true, plain.get("is_available"));

        Map<String, Object> missed = CalendarData.row(new CalendarView.Day(DAY - 1, -1, CalendarView.Status.MISSED, 0), SETTINGS);
        assertEquals("missed", missed.get("status"));
        assertEquals(false, missed.get("has_reward"));
    }

    @Test
    void paramsAreReadAsNumbersOrText() {
        assertEquals(3, CalendarData.param(Map.of("past", 3), "past", 7));
        assertEquals(4, CalendarData.param(Map.of("past", " 4 "), "past", 7));
        assertEquals(7, CalendarData.param(Map.of("past", "x"), "past", 7));
        assertEquals(7, CalendarData.param(Map.of(), "past", 7));
    }

    @Test
    void aMilestoneDayCarriesItsCountAndTheFirstIcon() {
        CalendarSettings settings = new CalendarSettings(SETTINGS.resetTime(), 7, CalendarSettings.AutoClaim.OFF, 0, true,
                7, 2, Map.of(7, DIAMONDS), null, List.of(Milestone.every(5, 5, null, null),
                Milestone.at(10, Map.of("material", "NETHER_STAR"), null)));
        Map<String, Object> row = CalendarData.row(new CalendarView.Day(DAY + 3, 3, CalendarView.Status.UPCOMING, 3, 10, true), settings);
        assertEquals(true, row.get("is_milestone"));
        assertEquals(10, row.get("streak"));
        assertEquals(2, row.get("milestone_count"));
        assertEquals(Map.of("material", "NETHER_STAR"), row.get("milestone_icon"));
        Map<String, Object> plain = CalendarData.row(new CalendarView.Day(DAY + 1, 1, CalendarView.Status.UPCOMING, 1, 8, false), settings);
        assertEquals(false, plain.get("is_milestone"));
        assertEquals(0, plain.get("milestone_count"));
    }

    @Test
    void aMilestoneRowSaysWhenAndWhetherItRepeats() {
        Map<String, Object> row = CalendarData.milestoneRow(
                new DailyService.UpcomingMilestone(60, 4, Milestone.every(30, 30, null, null)), 2, DAY);
        assertEquals("60-2", row.get("id"));
        assertEquals(4, row.get("offset"));
        assertEquals("29/10", row.get("date"));
        assertEquals(true, row.get("repeats"));
        assertEquals(30, row.get("every"));
        assertEquals(CalendarData.NO_REWARD_ICON, row.get("icon"));
    }
}
