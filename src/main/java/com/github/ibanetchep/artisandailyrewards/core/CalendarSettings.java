package com.github.ibanetchep.artisandailyrewards.core;

import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The settings of {@code data/settings.yml} and {@code data/rewards.yml} ({@code CalendarFiles}) the streak rule, the claim flow and the calendar
 * menu read. The rewards are one per day of the cycle ({@code days}, keyed by the day's number,
 * else {@code default_day}), plus the {@code milestones} of the streak, paid on top of the day's
 * reward ({@link Milestone}). The restore settings come with the next steps, and this reader
 * ignores them.
 *
 * @param minPlaytimeMinutes playtime of the current session before the day counts for the streak
 *                           and its reward can be claimed
 * @param notifyOnUnlock     tell a player when today's reward is unlocked, and remind at join of the
 *                           rewards of the days before that wait ({@code notify_on_unlock}, formerly
 *                           {@code join_reminder})
 * @param days               the reward of each listed day of the cycle, 1 to {@code cycleLength}
 * @param defaultDay         the reward of a day {@code days} does not list, null for none
 * @param milestones         the bonuses of the streak, in the file's order
 * @param withDefaultDay     a day with its own reward also pays {@code default_day}, unless the
 *                           day says otherwise ({@link DayReward#withDefault})
 * @param freezeStarting     freezes a player holds on their first counted day, at most
 *                           {@code freezeMaxStock} (a higher value is lowered to it)
 */
public record CalendarSettings(
        LocalTime resetTime,
        int cycleLength,
        AutoClaim autoClaim,
        int minPlaytimeMinutes,
        boolean notifyOnUnlock,
        int freezeEarnEvery,
        int freezeMaxStock,
        Map<Integer, DayReward> days,
        @Nullable DayReward defaultDay,
        List<Milestone> milestones,
        boolean withDefaultDay,
        int freezeStarting
) {

    public static final CalendarSettings DEFAULTS =
            new CalendarSettings(LocalTime.MIDNIGHT, 28, AutoClaim.OFF, 0, true, 7, 2, Map.of(), null, List.of(), false, 0);

    public CalendarSettings {
        days = Map.copyOf(days);
        milestones = List.copyOf(milestones);
        freezeStarting = Math.max(0, Math.min(freezeStarting, freezeMaxStock));
    }

    /** No freeze at the start. */
    public CalendarSettings(LocalTime resetTime, int cycleLength, AutoClaim autoClaim, int minPlaytimeMinutes,
                            boolean notifyOnUnlock, int freezeEarnEvery, int freezeMaxStock,
                            Map<Integer, DayReward> days, @Nullable DayReward defaultDay, List<Milestone> milestones,
                            boolean withDefaultDay) {
        this(resetTime, cycleLength, autoClaim, minPlaytimeMinutes, notifyOnUnlock, freezeEarnEvery, freezeMaxStock,
                days, defaultDay, milestones, withDefaultDay, 0);
    }

    /** These settings with {@code value} freezes at the start. */
    public CalendarSettings withFreezeStarting(int value) {
        return new CalendarSettings(resetTime, cycleLength, autoClaim, minPlaytimeMinutes, notifyOnUnlock,
                freezeEarnEvery, freezeMaxStock, days, defaultDay, milestones, withDefaultDay, value);
    }

    /** A day's own reward in place of {@code default_day}. */
    public CalendarSettings(LocalTime resetTime, int cycleLength, AutoClaim autoClaim, int minPlaytimeMinutes,
                            boolean notifyOnUnlock, int freezeEarnEvery, int freezeMaxStock,
                            Map<Integer, DayReward> days, @Nullable DayReward defaultDay, List<Milestone> milestones) {
        this(resetTime, cycleLength, autoClaim, minPlaytimeMinutes, notifyOnUnlock, freezeEarnEvery, freezeMaxStock,
                days, defaultDay, milestones, false);
    }

    /** Without milestones. */
    public CalendarSettings(LocalTime resetTime, int cycleLength, AutoClaim autoClaim, int minPlaytimeMinutes,
                            boolean notifyOnUnlock, int freezeEarnEvery, int freezeMaxStock,
                            Map<Integer, DayReward> days, @Nullable DayReward defaultDay) {
        this(resetTime, cycleLength, autoClaim, minPlaytimeMinutes, notifyOnUnlock, freezeEarnEvery, freezeMaxStock,
                days, defaultDay, List.of());
    }

    /** The milestones a streak of {@code streak} pays, in the file's order. */
    public List<Milestone> milestonesAt(int streak) {
        return Milestone.paidAt(milestones, streak);
    }

    /**
     * A day's reward, kept as read: Artisan interprets both.
     *
     * @param icon    the item value the menu shows ({@code item: "{d.icon}"}), null for none
     * @param actions the action chain a claim of that day runs, null for none
     * @param withDefault for a day of {@code days}: also pay {@code default_day} (true), or not
     *                    (false); null follows {@link #withDefaultDay}
     */
    public record DayReward(@Nullable Object icon, @Nullable Object actions, @Nullable Boolean withDefault) {

        public DayReward(@Nullable Object icon, @Nullable Object actions) {
            this(icon, actions, null);
        }
    }

    /** The reward a day of the cycle shows: its own, else {@link #defaultDay}, else null. */
    public @Nullable DayReward reward(int cycleDay) {
        DayReward own = days.get(cycleDay);
        return own != null ? own : defaultDay;
    }

    /**
     * What a claim of a day of the cycle pays, in order: {@link #defaultDay} then the day's own
     * when the day stacks them ({@link #stacksDefault}), else its own, else {@link #defaultDay};
     * empty for none.
     */
    public List<DayReward> rewards(int cycleDay) {
        DayReward own = days.get(cycleDay);
        if (own == null) return defaultDay != null ? List.of(defaultDay) : List.of();
        return defaultDay != null && stacksDefault(own) ? List.of(defaultDay, own) : List.of(own);
    }

    /** True when the day's own reward is paid with {@code default_day}, not in its place. */
    public boolean stacksDefault(DayReward own) {
        return own.withDefault() != null ? own.withDefault() : withDefaultDay;
    }

    /** The key, at the top of the rewards and under a day, that pays {@code default_day} with a day's own reward. */
    public static final String WITH_DEFAULT_DAY = "with_default_day";

    public enum AutoClaim {
        /** The player claims: {@code /dailyrewards claim} or the menu. */
        OFF,
        /** The claim goes on its own on join and at the reset. */
        ON,
        /** Each player chooses with {@code /dailyrewards autoclaim}. */
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
        // join_reminder: its name before notify_on_unlock, still read when the new one is absent.
        boolean notifyOnUnlock = bool(root, "notify_on_unlock", bool(root, "join_reminder", d.notifyOnUnlock()));

        int earnEvery = d.freezeEarnEvery();
        int maxStock = d.freezeMaxStock();
        int starting = d.freezeStarting();
        Object freeze = root.get("freeze");
        if (freeze instanceof Map<?, ?> map) {
            earnEvery = integer(map, "earn_every", earnEvery, 0);
            maxStock = integer(map, "max_stock", maxStock, 0);
            starting = integer(map, "starting", starting, 0);
        } else if (freeze != null) {
            throw new CalendarException("freeze: expected a section");
        }
        Map<Integer, DayReward> days = days(root.get("days"), cycleLength);
        Object defaultSection = root.get("default_day");
        DayReward defaultDay = defaultSection != null ? reward("default_day", defaultSection) : null;
        if (defaultDay != null && defaultDay.withDefault() != null) {
            throw new CalendarException("default_day: " + WITH_DEFAULT_DAY + " only goes under days or at the top of the file");
        }
        List<Milestone> milestones = Milestone.read(root.get("milestones"));
        boolean withDefaultDay = bool(root, WITH_DEFAULT_DAY, d.withDefaultDay());
        return new CalendarSettings(resetTime, cycleLength, autoClaim, minPlaytime, notifyOnUnlock,
                earnEvery, maxStock, days, defaultDay, milestones, withDefaultDay, starting);
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

    /**
     * A section with an icon, actions or both, and its {@code with_default_day}; null when it has
     * neither icon nor actions (the editor's empty form).
     */
    private static @Nullable DayReward reward(String key, Object value) throws CalendarException {
        if (!(value instanceof Map<?, ?> map)) throw new CalendarException(key + ": expected a section with icon and actions");
        Object icon = Icons.withoutBlankTitle(map.get("icon"));
        Object actions = map.get("actions");
        Object with = map.get(WITH_DEFAULT_DAY);
        if (with != null && !(with instanceof Boolean)) {
            throw new CalendarException(key + "." + WITH_DEFAULT_DAY + ": \"" + with + "\" is not true or false");
        }
        return icon == null && actions == null ? null : new DayReward(icon, actions, (Boolean) with);
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
