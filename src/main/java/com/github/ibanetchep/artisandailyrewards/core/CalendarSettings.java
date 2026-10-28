package com.github.ibanetchep.artisandailyrewards.core;

import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The settings of {@code data/calendar.yaml} the streak rule, the claim flow and the calendar
 * menu read. The rewards are one per day of the cycle ({@code days}, keyed by the day's number,
 * else {@code default_day}); the base reward, the tiers, the milestones and the restore settings
 * come with the next steps, and this reader ignores them.
 *
 * @param minPlaytimeMinutes playtime of the current session before the day counts for the streak
 *                           and its reward can be claimed
 * @param joinReminder       remind a player whose rewards wait, on join and at the reset
 * @param days               the reward of each listed day of the cycle, 1 to {@code cycleLength}
 * @param defaultDay         the reward of a day {@code days} does not list, null for none
 */
public record CalendarSettings(
        LocalTime resetTime,
        int cycleLength,
        AutoClaim autoClaim,
        int minPlaytimeMinutes,
        boolean joinReminder,
        int freezeEarnEvery,
        int freezeMaxStock,
        Map<Integer, DayReward> days,
        @Nullable DayReward defaultDay
) {

    public static final CalendarSettings DEFAULTS =
            new CalendarSettings(LocalTime.MIDNIGHT, 28, AutoClaim.OFF, 0, true, 7, 2, Map.of(), null);

    public CalendarSettings {
        days = Map.copyOf(days);
    }

    /**
     * A day's reward, kept as read: Artisan interprets both.
     *
     * @param icon    the item value the menu shows ({@code item: "{d.icon}"}), null for none
     * @param actions the action chain a claim of that day runs, null for none
     */
    public record DayReward(@Nullable Object icon, @Nullable Object actions) {
    }

    /** The reward of a day of the cycle: its own, else {@link #defaultDay}, else null. */
    public @Nullable DayReward reward(int cycleDay) {
        DayReward own = days.get(cycleDay);
        return own != null ? own : defaultDay;
    }

    public enum AutoClaim {
        /** The player claims: {@code /daily claim} or the menu. */
        OFF,
        /** The claim goes on its own on join and at the reset. */
        ON,
        /** Each player chooses with {@code /daily autoclaim}. */
        PLAYER
    }

    /**
     * Reads the parsed YAML of the calendar. An absent key keeps its default; a wrong one is an
     * error, and the caller keeps the previous settings.
     */
    public static CalendarSettings read(Map<?, ?> root) throws CalendarException {
        CalendarSettings d = DEFAULTS;
        LocalTime resetTime = d.resetTime();
        Object reset = root.get("reset_time");
        if (reset != null) {
            try {
                resetTime = LocalTime.parse(String.valueOf(reset));
            } catch (DateTimeParseException e) {
                throw new CalendarException("reset_time: \"" + reset + "\" is not a time like \"00:00\"");
            }
        }
        int cycleLength = integer(root, "cycle_length", d.cycleLength(), 1);
        AutoClaim autoClaim = autoClaim(root.get("auto_claim"), d.autoClaim());
        int minPlaytime = integer(root, "min_playtime_minutes", d.minPlaytimeMinutes(), 0);
        boolean joinReminder = bool(root, "join_reminder", d.joinReminder());

        int earnEvery = d.freezeEarnEvery();
        int maxStock = d.freezeMaxStock();
        Object freeze = root.get("freeze");
        if (freeze instanceof Map<?, ?> map) {
            earnEvery = integer(map, "earn_every", earnEvery, 0);
            maxStock = integer(map, "max_stock", maxStock, 0);
        } else if (freeze != null) {
            throw new CalendarException("freeze: expected a section");
        }
        Map<Integer, DayReward> days = days(root.get("days"), cycleLength);
        Object defaultSection = root.get("default_day");
        DayReward defaultDay = defaultSection != null ? reward("default_day", defaultSection) : null;
        return new CalendarSettings(resetTime, cycleLength, autoClaim, minPlaytime, joinReminder,
                earnEvery, maxStock, days, defaultDay);
    }

    /** {@code days}: a section keyed by day number, each within the cycle. */
    private static Map<Integer, DayReward> days(Object value, int cycleLength) throws CalendarException {
        if (value == null) return Map.of();
        if (!(value instanceof Map<?, ?> map)) throw new CalendarException("days: expected a section keyed by day number");
        Map<Integer, DayReward> days = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            int day;
            try {
                day = Integer.parseInt(String.valueOf(entry.getKey()).trim());
            } catch (NumberFormatException e) {
                throw new CalendarException("days: \"" + entry.getKey() + "\" is not a day number");
            }
            if (day < 1 || day > cycleLength) {
                throw new CalendarException("days: " + day + " is outside the cycle, 1 to " + cycleLength + " (cycle_length)");
            }
            if (days.containsKey(day)) throw new CalendarException("days: " + day + " is listed twice");
            DayReward reward = reward("days." + day, entry.getValue());
            if (reward != null) days.put(day, reward);
        }
        return days;
    }

    /** A section with an icon, actions or both; null when it has neither (the editor's empty form). */
    private static @Nullable DayReward reward(String key, Object value) throws CalendarException {
        if (!(value instanceof Map<?, ?> map)) throw new CalendarException(key + ": expected a section with icon and actions");
        Object icon = map.get("icon");
        Object actions = map.get("actions");
        return icon == null && actions == null ? null : new DayReward(icon, actions);
    }

    /** YAML 1.1 reads a bare {@code off} or {@code on} as a boolean: both spellings are accepted. */
    private static AutoClaim autoClaim(Object value, AutoClaim fallback) throws CalendarException {
        if (value == null) return fallback;
        if (value instanceof Boolean b) return b ? AutoClaim.ON : AutoClaim.OFF;
        String text = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        return switch (text) {
            case "off", "false", "no" -> AutoClaim.OFF;
            case "on", "true", "yes" -> AutoClaim.ON;
            case "player" -> AutoClaim.PLAYER;
            default -> throw new CalendarException("auto_claim: \"" + value + "\" is not one of " + List.of("off", "on", "player"));
        };
    }

    private static int integer(Map<?, ?> map, String key, int fallback, int min) throws CalendarException {
        Object value = map.get(key);
        if (value == null) return fallback;
        int parsed;
        if (value instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue())) {
            parsed = n.intValue();
        } else {
            try {
                parsed = Integer.parseInt(String.valueOf(value).trim());
            } catch (NumberFormatException e) {
                throw new CalendarException(key + ": \"" + value + "\" is not a whole number");
            }
        }
        if (parsed < min) throw new CalendarException(key + ": " + parsed + " is below " + min);
        return parsed;
    }

    private static boolean bool(Map<?, ?> map, String key, boolean fallback) throws CalendarException {
        Object value = map.get(key);
        if (value == null) return fallback;
        if (value instanceof Boolean b) return b;
        throw new CalendarException(key + ": \"" + value + "\" is not true or false");
    }
}
