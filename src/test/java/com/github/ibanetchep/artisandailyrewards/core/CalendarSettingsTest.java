package com.github.ibanetchep.artisandailyrewards.core;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarSettingsTest {

    @Test
    void anEmptyCalendarKeepsTheDefaults() throws Exception {
        assertEquals(CalendarSettings.DEFAULTS, CalendarSettings.read(Map.of()));
    }

    @Test
    void readsEveryKey() throws Exception {
        CalendarSettings settings = CalendarSettings.read(Map.of(
                "reset_time", "06:00",
                "cycle_length", 7,
                "auto_claim", "player",
                "min_playtime_minutes", 5,
                "join_reminder", false,
                "freeze", Map.of("earn_every", 10, "max_stock", 3)));
        assertEquals(LocalTime.of(6, 0), settings.resetTime());
        assertEquals(7, settings.cycleLength());
        assertEquals(CalendarSettings.AutoClaim.PLAYER, settings.autoClaim());
        assertEquals(5, settings.minPlaytimeMinutes());
        assertFalse(settings.joinReminder());
        assertEquals(10, settings.freezeEarnEvery());
        assertEquals(3, settings.freezeMaxStock());
    }

    @Test
    void eachDayHasItsRewardElseTheDefaultOne() throws Exception {
        Map<String, Object> bread = Map.of("icon", Map.of("material", "BREAD"), "actions", List.of(Map.of("give_item", "BREAD")));
        Map<String, Object> diamond = Map.of("icon", "DIAMOND");
        CalendarSettings settings = CalendarSettings.read(Map.of(
                "cycle_length", 7,
                "default_day", bread,
                "days", Map.of(7, diamond, "3", Map.of("actions", List.of()))));
        assertEquals("DIAMOND", settings.reward(7).icon());
        assertNull(settings.reward(7).actions());
        assertNull(settings.reward(3).icon());
        assertEquals(Map.of("material", "BREAD"), settings.reward(1).icon());
        assertNull(CalendarSettings.DEFAULTS.reward(1));
        // An empty section, as the editor's form leaves it: no reward.
        assertNull(CalendarSettings.read(Map.of("default_day", Map.of(), "days", Map.of(1, Map.of()))).reward(1));
    }

    @Test
    void anIconWithABlankTitleKeepsTheItemsOwnName() throws Exception {
        Map<String, Object> iron = Map.of("icon", Map.of("material", "IRON_INGOT", "title", ""));
        Map<String, Object> custom = Map.of("icon", Map.of("extends", "item:ci_1", "title", Map.of("en", " ", "fr", "")));
        Map<String, Object> named = Map.of("icon", Map.of("material", "DIAMOND", "title", "<aqua>Diamonds"));
        CalendarSettings settings = CalendarSettings.read(Map.of(
                "cycle_length", 7,
                "default_day", custom,
                "days", Map.of(7, iron, 6, named),
                "milestones", List.of(Map.of("at", 30, "icon", Map.of("material", "APPLE", "title", "")))));
        assertEquals(Map.of("material", "IRON_INGOT"), settings.reward(7).icon());
        assertEquals(Map.of("extends", "item:ci_1"), settings.reward(1).icon());
        assertEquals(Map.of("material", "DIAMOND", "title", "<aqua>Diamonds"), settings.reward(6).icon());
        assertEquals(Map.of("material", "APPLE"), settings.milestones().get(0).icon());
    }

    @Test
    void aDayReplacesTheDefaultOneUnlessItStacksIt() throws Exception {
        Map<String, Object> bread = Map.of("icon", "BREAD");
        Map<String, Object> diamond = Map.of("icon", "DIAMOND");
        Map<String, Object> gold = Map.of("icon", "GOLD_INGOT", "with_default_day", true);
        Map<String, Object> iron = Map.of("icon", "IRON_INGOT", "with_default_day", false);
        Map<Integer, Object> days = Map.of(2, diamond, 3, gold, 4, iron);

        CalendarSettings replaces = CalendarSettings.read(Map.of("cycle_length", 7, "default_day", bread, "days", days));
        assertFalse(replaces.withDefaultDay());
        assertEquals(List.of("BREAD"), icons(replaces.rewards(1)));
        assertEquals(List.of("DIAMOND"), icons(replaces.rewards(2)));
        assertEquals(List.of("BREAD", "GOLD_INGOT"), icons(replaces.rewards(3)));
        assertEquals(List.of("IRON_INGOT"), icons(replaces.rewards(4)));

        CalendarSettings stacks = CalendarSettings.read(Map.of("cycle_length", 7, "default_day", bread, "days", days,
                "with_default_day", true));
        assertEquals(List.of("BREAD"), icons(stacks.rewards(1)));
        assertEquals(List.of("BREAD", "DIAMOND"), icons(stacks.rewards(2)));
        assertEquals(List.of("BREAD", "GOLD_INGOT"), icons(stacks.rewards(3)));
        assertEquals(List.of("IRON_INGOT"), icons(stacks.rewards(4)));
        // The menu shows the day's own icon either way.
        assertEquals("DIAMOND", stacks.reward(2).icon());

        // No default_day: nothing to stack.
        CalendarSettings alone = CalendarSettings.read(Map.of("days", Map.of(1, gold), "with_default_day", true));
        assertEquals(List.of("GOLD_INGOT"), icons(alone.rewards(1)));
        assertTrue(alone.rewards(2).isEmpty());
    }

    @Test
    void withDefaultDayIsABoolean() {
        assertTrue(assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of(
                "with_default_day", "yes"))).getMessage().startsWith("with_default_day"));
        assertTrue(assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of(
                "days", Map.of(1, Map.of("icon", "DIAMOND", "with_default_day", "yes"))))).getMessage()
                .startsWith("days.1.with_default_day"));
        assertTrue(assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of(
                "default_day", Map.of("icon", "BREAD", "with_default_day", true)))).getMessage()
                .startsWith("default_day"));
    }

    private static List<Object> icons(List<CalendarSettings.DayReward> rewards) {
        return rewards.stream().map(CalendarSettings.DayReward::icon).toList();
    }

    @Test
    void aDayOutsideTheCycleIsRefused() {
        assertTrue(assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of(
                "cycle_length", 7, "days", Map.of(8, Map.of())))).getMessage().startsWith("days"));
        assertTrue(assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of(
                "days", Map.of("first", Map.of())))).getMessage().startsWith("days"));
        assertTrue(assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of(
                "days", Map.of(1, "BREAD")))).getMessage().startsWith("days.1"));
        assertTrue(assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of(
                "days", List.of()))).getMessage().startsWith("days"));
    }

    @Test
    void aBareOffOrOnIsReadAsYamlBooleans() throws Exception {
        assertEquals(CalendarSettings.AutoClaim.OFF, CalendarSettings.read(Map.of("auto_claim", false)).autoClaim());
        assertEquals(CalendarSettings.AutoClaim.ON, CalendarSettings.read(Map.of("auto_claim", true)).autoClaim());
        assertEquals(CalendarSettings.AutoClaim.ON, CalendarSettings.read(Map.of("auto_claim", "ON")).autoClaim());
    }

    @Test
    void aWrongValueNamesItsKey() {
        assertTrue(assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of("cycle_length", 0)))
                .getMessage().startsWith("cycle_length"));
        assertTrue(assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of("reset_time", "6h")))
                .getMessage().startsWith("reset_time"));
        assertTrue(assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of("auto_claim", "sometimes")))
                .getMessage().startsWith("auto_claim"));
        assertTrue(assertThrows(CalendarException.class, () -> CalendarSettings.read(Map.of("freeze", 3)))
                .getMessage().startsWith("freeze"));
    }
}
