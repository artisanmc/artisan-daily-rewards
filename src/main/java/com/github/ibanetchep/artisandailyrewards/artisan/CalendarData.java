package com.github.ibanetchep.artisandailyrewards.artisan;

import com.github.ibanetchep.artisandailyrewards.DailyService;
import com.github.ibanetchep.artisandailyrewards.core.CalendarSettings;
import com.github.ibanetchep.artisandailyrewards.core.CalendarView;
import com.github.ibanetchep.artisandailyrewards.core.Durations;
import com.github.ibanetchep.artisandailyrewards.core.PlayerState;
import com.github.ibanetchep.artisandailyrewards.core.StreakRules;
import net.artisanmc.modules.api.datasource.DataSourceDeclaration;
import net.artisanmc.modules.api.datasource.DataSourceField;
import net.artisanmc.modules.api.model.ModelFieldType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * What the calendar menu shows, from the service's cache (no I/O, any thread):
 * <ul>
 *   <li>{@code daily:calendar}, one row per game day: the recent ones as they went, today, and
 *       the days to come with the reward each one will pay. Params {@code past} (7 by default, at
 *       most {@link DailyService#HISTORY_DAYS}) and {@code future} (20);</li>
 *   <li>{@code daily:status}, one row: the viewer's streak, jokers and today's claim.</li>
 * </ul>
 */
public final class CalendarData {

    public static final String CALENDAR = "daily:calendar";
    public static final String STATUS = "daily:status";

    static final int DEFAULT_PAST = 7;
    static final int DEFAULT_FUTURE = 20;
    /** A day without a reward still has an item to show. */
    static final Map<String, Object> NO_REWARD_ICON = Map.of("material", "PAPER");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM", Locale.ROOT);

    private final DailyService service;

    public CalendarData(DailyService service) {
        this.service = service;
    }

    public DataSourceDeclaration calendar() {
        return new DataSourceDeclaration(CALENDAR, (player, params) -> calendar(player, params), List.of(
                new DataSourceField("id", ModelFieldType.string(), "The game day's date, yyyy-MM-dd"),
                new DataSourceField("date", ModelFieldType.string(), "dd/MM"),
                new DataSourceField("offset", ModelFieldType.integer(), "Days from today: -1 yesterday, 0 today, 1 tomorrow"),
                new DataSourceField("status", ModelFieldType.enumOf(List.of("before", "claimed", "missed", "available", "upcoming")),
                        "before: earlier than the first claim. available: today, not claimed yet"),
                new DataSourceField("is_today", ModelFieldType.bool(), null),
                new DataSourceField("is_claimed", ModelFieldType.bool(), "Claimed, today or before"),
                new DataSourceField("is_missed", ModelFieldType.bool(), "Over and not claimed (a joker may have kept the streak)"),
                new DataSourceField("is_before", ModelFieldType.bool(), "Before the viewer's first claim"),
                new DataSourceField("is_available", ModelFieldType.bool(), "Today, waiting to be claimed"),
                new DataSourceField("is_upcoming", ModelFieldType.bool(), "A day to come"),
                new DataSourceField("cycle_day", ModelFieldType.integer(),
                        "The day of the cycle paid or to be paid if the viewer claims every day; 0 when missed or before"),
                new DataSourceField("has_reward", ModelFieldType.bool(), "That day of the cycle has a reward"),
                new DataSourceField("icon", ModelFieldType.item(),
                        "The reward's icon (days, else default_day of the calendar), for item: \"{d.icon}\"")));
    }

    public DataSourceDeclaration status() {
        return new DataSourceDeclaration(STATUS, (player, params) -> status(player), List.of(
                new DataSourceField("id", ModelFieldType.string(), "Always status"),
                new DataSourceField("loaded", ModelFieldType.bool(), "The viewer's streak is loaded (false for a moment after a join)"),
                new DataSourceField("streak", ModelFieldType.integer(), "As shown: 0 once broken"),
                new DataSourceField("longest", ModelFieldType.integer(), null),
                new DataSourceField("total", ModelFieldType.integer(), "Claims ever"),
                new DataSourceField("freezes", ModelFieldType.integer(), "Jokers held"),
                new DataSourceField("freeze_max", ModelFieldType.integer(), "Jokers held at most"),
                new DataSourceField("claimed_today", ModelFieldType.bool(), null),
                new DataSourceField("can_claim", ModelFieldType.bool(), "Loaded and not claimed today"),
                new DataSourceField("at_risk", ModelFieldType.bool(), "Days missed that the jokers will cover at the next claim"),
                new DataSourceField("broken", ModelFieldType.bool(), "The streak starts again at the next claim"),
                new DataSourceField("lost", ModelFieldType.integer(), "The streak a break loses, 0 when not broken"),
                new DataSourceField("cycle_day", ModelFieldType.integer(), "Today's day of the cycle, claimed or to claim"),
                new DataSourceField("cycle_length", ModelFieldType.integer(), null),
                new DataSourceField("until_reset", ModelFieldType.string(), "Time left in the game day: 3h 12m")));
    }

    List<Map<String, Object>> calendar(@Nullable Player player, Map<String, Object> params) {
        if (player == null) return List.of();
        List<CalendarView.Day> days = service.calendar(player.getUniqueId(),
                param(params, "past", DEFAULT_PAST), param(params, "future", DEFAULT_FUTURE));
        if (days == null) return List.of();
        CalendarSettings settings = service.settings();
        return days.stream().map(day -> row(day, settings)).toList();
    }

    static Map<String, Object> row(CalendarView.Day day, CalendarSettings settings) {
        CalendarSettings.DayReward reward = day.cycleDay() > 0 ? settings.reward(day.cycleDay()) : null;
        Object icon = reward != null && reward.icon() != null ? reward.icon() : NO_REWARD_ICON;
        LocalDate date = LocalDate.ofEpochDay(day.day());
        Map<String, Object> row = new HashMap<>();
        row.put("id", date.toString());
        row.put("date", DATE.format(date));
        row.put("offset", day.offset());
        row.put("status", day.status().name().toLowerCase(Locale.ROOT));
        row.put("is_today", day.offset() == 0);
        row.put("is_claimed", day.status() == CalendarView.Status.CLAIMED);
        row.put("is_missed", day.status() == CalendarView.Status.MISSED);
        row.put("is_before", day.status() == CalendarView.Status.BEFORE);
        row.put("is_available", day.status() == CalendarView.Status.AVAILABLE);
        row.put("is_upcoming", day.status() == CalendarView.Status.UPCOMING);
        row.put("cycle_day", day.cycleDay());
        row.put("has_reward", reward != null);
        row.put("icon", icon);
        return row;
    }

    List<Map<String, Object>> status(@Nullable Player player) {
        PlayerState state = player != null ? service.state(player.getUniqueId()) : null;
        CalendarSettings settings = service.settings();
        Map<String, Object> row = new HashMap<>();
        row.put("id", "status");
        row.put("loaded", state != null);
        row.put("freeze_max", settings.freezeMaxStock());
        row.put("cycle_length", settings.cycleLength());
        row.put("until_reset", Durations.format(service.clock().untilReset()));
        if (state == null) {
            row.putAll(Map.of("streak", 0, "longest", 0, "total", 0, "freezes", 0, "claimed_today", false,
                    "can_claim", false, "at_risk", false, "broken", false, "lost", 0, "cycle_day", 1));
            return List.of(row);
        }
        StreakRules.Displayed shown = service.displayed(state);
        int todayStreak = shown.claimedToday() ? shown.streak() : shown.streak() + 1;
        row.put("streak", shown.streak());
        row.put("longest", state.longest());
        row.put("total", state.totalClaims());
        row.put("freezes", state.freezes());
        row.put("claimed_today", shown.claimedToday());
        row.put("can_claim", !shown.claimedToday());
        row.put("at_risk", shown.atRisk());
        row.put("broken", shown.broken());
        row.put("lost", shown.broken() ? state.streak() : 0);
        row.put("cycle_day", StreakRules.cycleDay(todayStreak, settings.cycleLength()));
        return List.of(row);
    }

    /** A whole number of {@code params}, else {@code fallback}: menus pass them as text or numbers. */
    static int param(Map<String, Object> params, String key, int fallback) {
        Object value = params.get(key);
        if (value instanceof Number n) return n.intValue();
        if (value == null) return fallback;
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
