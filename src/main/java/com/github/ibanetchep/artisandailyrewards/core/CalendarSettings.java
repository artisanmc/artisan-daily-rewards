package com.github.ibanetchep.artisandailyrewards.core;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The settings of {@code data/calendar.yaml} the streak rule and the claim flow read. The
 * rewards (days, base, tiers, milestones) and the restore settings are read with the Artisan
 * integration; this reader ignores them.
 *
 * @param minPlaytimeMinutes playtime of the current session before a claim is accepted
 * @param joinReminder       remind a player who has not claimed, on join and at the reset
 * @param menu               the Artisan menu {@code /daily} opens
 */
public record CalendarSettings(
        LocalTime resetTime,
        int cycleLength,
        AutoClaim autoClaim,
        int minPlaytimeMinutes,
        boolean joinReminder,
        String menu,
        int freezeEarnEvery,
        int freezeMaxStock
) {

    public static final CalendarSettings DEFAULTS =
            new CalendarSettings(LocalTime.MIDNIGHT, 28, AutoClaim.OFF, 0, true, "daily", 7, 2);

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
        Object menu = root.get("menu");
        String menuRef = menu != null ? String.valueOf(menu).trim() : d.menu();
        if (menuRef.isEmpty()) throw new CalendarException("menu: empty");

        int earnEvery = d.freezeEarnEvery();
        int maxStock = d.freezeMaxStock();
        Object freeze = root.get("freeze");
        if (freeze instanceof Map<?, ?> map) {
            earnEvery = integer(map, "earn_every", earnEvery, 0);
            maxStock = integer(map, "max_stock", maxStock, 0);
        } else if (freeze != null) {
            throw new CalendarException("freeze: expected a section");
        }
        return new CalendarSettings(resetTime, cycleLength, autoClaim, minPlaytime, joinReminder, menuRef,
                earnEvery, maxStock);
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
