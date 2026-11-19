package com.github.ibanetchep.artisandailyrewards.artisan;

import com.github.ibanetchep.artisandailyrewards.DailyService;
import com.github.ibanetchep.artisandailyrewards.core.CalendarSettings;
import com.github.ibanetchep.artisandailyrewards.core.CalendarView;
import com.github.ibanetchep.artisandailyrewards.core.Durations;
import com.github.ibanetchep.artisandailyrewards.core.Milestone;
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
import java.util.Objects;

/**
 * What the calendar menu shows, from the service's cache (no I/O, any thread):
 * <ul>
 *   <li>{@code daily:calendar}, one row per game day: the recent ones as they went (claimed,
 *       waiting to be claimed, missed), today, and
 *       the days to come with the reward each one will pay. Params {@code past} (7 by default, at
 *       most {@link DailyService#HISTORY_DAYS}), {@code future} (20) and {@code week} (0: none; 7
 *       moves days from the end to the start so the first day shown sits on the first column of
 *       a 7-wide grid, today on the column of its day of the cycle, as the Rewards grid);</li>
 *   <li>{@code daily:status}, one row: the viewer's streak, jokers, today's claim and the next
 *       milestone;</li>
 *   <li>{@code daily:milestones}, one row per coming milestone, the next ones the viewer reaches
 *       if they come every day. Param {@code count} (28 by default, at most
 *       {@link DailyService#MAX_MILESTONES}).</li>
 * </ul>
 */
public final class CalendarData {

    public static final String CALENDAR = "daily:calendar";
    public static final String STATUS = "daily:status";
    public static final String MILESTONES = "daily:milestones";

    static final int DEFAULT_PAST = 7;
    static final int DEFAULT_FUTURE = 20;
    static final int DEFAULT_MILESTONES = 28;
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
                        "before: earlier than the first day counted. available: a reward to claim, today or a day the viewer was there"),
                new DataSourceField("is_today", ModelFieldType.bool(), null),
                new DataSourceField("is_claimed", ModelFieldType.bool(), "Its reward claimed, today or before"),
                new DataSourceField("is_missed", ModelFieldType.bool(), "Over and the viewer was not there (a joker may have kept the streak)"),
                new DataSourceField("is_before", ModelFieldType.bool(), "Before the viewer's first day counted"),
                new DataSourceField("is_available", ModelFieldType.bool(), "Its reward waits: today, or a day the viewer was there without claiming"),
                new DataSourceField("is_upcoming", ModelFieldType.bool(), "A day to come"),
                new DataSourceField("cycle_day", ModelFieldType.integer(),
                        "The day of the cycle paid or to be paid if the viewer comes every day; 0 when missed or before"),
                new DataSourceField("has_reward", ModelFieldType.bool(), "That day of the cycle has a reward"),
                new DataSourceField("icon", ModelFieldType.item(),
                        "The reward's icon (days, else default_day of the calendar), for item: \"{d.icon}\""),
                new DataSourceField("streak", ModelFieldType.integer(),
                        "The streak that day reaches (today and to come: if the viewer comes every day); before today, 0 unless it paid milestones"),
                new DataSourceField("is_milestone", ModelFieldType.bool(),
                        "The day pays milestones on top of its reward: paid, waiting, or to come if the viewer comes every day"),
                new DataSourceField("milestone_count", ModelFieldType.integer(), "How many milestones the day pays, 0 for none"),
                new DataSourceField("milestone_icon", ModelFieldType.item(),
                        "The icon of the day's first milestone, else the day's icon")));
    }

    public DataSourceDeclaration milestones() {
        return new DataSourceDeclaration(MILESTONES, (player, params) -> milestones(player, params), List.of(
                new DataSourceField("id", ModelFieldType.string(), "The streak and the milestone's place in the file: 30-2"),
                new DataSourceField("streak", ModelFieldType.integer(), "The streak that pays it"),
                new DataSourceField("offset", ModelFieldType.integer(), "Days from today if the viewer comes every day: 0 today"),
                new DataSourceField("date", ModelFieldType.string(), "dd/MM, if the viewer comes every day"),
                new DataSourceField("is_today", ModelFieldType.bool(), null),
                new DataSourceField("repeats", ModelFieldType.bool(), "An every milestone, paid again and again"),
                new DataSourceField("every", ModelFieldType.integer(), "The step of a repeating milestone, 0 for an at one"),
                new DataSourceField("icon", ModelFieldType.item(), "The milestone's icon, for item: \"{m.icon}\"")));
    }

    public DataSourceDeclaration status() {
        return new DataSourceDeclaration(STATUS, (player, params) -> status(player), List.of(
                new DataSourceField("id", ModelFieldType.string(), "Always status"),
                new DataSourceField("loaded", ModelFieldType.bool(), "The viewer's streak is loaded (false for a moment after a join)"),
                new DataSourceField("streak", ModelFieldType.integer(), "As shown: 0 once broken"),
                new DataSourceField("longest", ModelFieldType.integer(), null),
                new DataSourceField("total", ModelFieldType.integer(), "Days counted ever"),
                new DataSourceField("freezes", ModelFieldType.integer(), "Jokers held"),
                new DataSourceField("freeze_max", ModelFieldType.integer(), "Jokers held at most"),
                new DataSourceField("claimed_today", ModelFieldType.bool(), "Today counted and its reward claimed"),
                new DataSourceField("unclaimed", ModelFieldType.integer(), "Rewards of days counted that wait to be claimed"),
                new DataSourceField("can_claim", ModelFieldType.bool(), "Loaded, and a reward waits or today is not claimed yet"),
                new DataSourceField("at_risk", ModelFieldType.bool(), "Days missed that the jokers will cover when the viewer is next there"),
                new DataSourceField("broken", ModelFieldType.bool(), "The streak starts again when the viewer is next there"),
                new DataSourceField("lost", ModelFieldType.integer(), "The streak a break loses, 0 when not broken"),
                new DataSourceField("cycle_day", ModelFieldType.integer(), "Today's day of the cycle, counted or to count"),
                new DataSourceField("cycle_length", ModelFieldType.integer(), null),
                new DataSourceField("until_reset", ModelFieldType.string(), "Time left in the game day: 3h 12m"),
                new DataSourceField("has_milestones", ModelFieldType.bool(),
                        "The server has milestones in data/rewards (the calendar shows its milestones button only then)"),
                new DataSourceField("next_milestone", ModelFieldType.integer(),
                        "The streak of the next milestone the viewer reaches, 0 when none is left"),
                new DataSourceField("next_milestone_in", ModelFieldType.integer(),
                        "Days until then if the viewer comes every day: 0 today, -1 when none is left")));
    }

    List<Map<String, Object>> calendar(@Nullable Player player, Map<String, Object> params) {
        if (player == null) return List.of();
        List<CalendarView.Day> days = service.calendar(player.getUniqueId(),
                param(params, "past", DEFAULT_PAST), param(params, "future", DEFAULT_FUTURE),
                param(params, "week", 0));
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
        row.put("streak", day.streak());
        List<Milestone> paid = day.milestone() ? settings.milestonesAt(day.streak()) : List.of();
        row.put("is_milestone", day.milestone());
        row.put("milestone_count", paid.size());
        row.put("milestone_icon", paid.stream().map(Milestone::icon).filter(Objects::nonNull).findFirst().orElse(icon));
        return row;
    }

    List<Map<String, Object>> milestones(@Nullable Player player, Map<String, Object> params) {
        if (player == null) return List.of();
        List<DailyService.UpcomingMilestone> coming = service.upcomingMilestones(player.getUniqueId(),
                param(params, "count", DEFAULT_MILESTONES));
        if (coming == null) return List.of();
        List<Milestone> all = service.settings().milestones();
        long today = service.clock().today();
        return coming.stream().map(m -> milestoneRow(m, all.indexOf(m.milestone()) + 1, today)).toList();
    }

    static Map<String, Object> milestoneRow(DailyService.UpcomingMilestone coming, int place, long today) {
        Milestone milestone = coming.milestone();
        LocalDate date = LocalDate.ofEpochDay(today + coming.offset());
        Map<String, Object> row = new HashMap<>();
        row.put("id", coming.streak() + "-" + place);
        row.put("streak", coming.streak());
        row.put("offset", coming.offset());
        row.put("date", DATE.format(date));
        row.put("is_today", coming.offset() == 0);
        row.put("repeats", milestone.repeats());
        row.put("every", milestone.every());
        row.put("icon", milestone.icon() != null ? milestone.icon() : NO_REWARD_ICON);
        return row;
    }

    List<Map<String, Object>> status(@Nullable Player player) {
        PlayerState state = player != null ? service.state(player.getUniqueId()) : null;
        CalendarSettings settings = service.settings();
        Map<String, Object> row = new HashMap<>();
        row.put("id", "status");
        row.put("loaded", state != null);
        row.put("freeze_max", settings.freezeMaxStock());
        row.put("has_milestones", !settings.milestones().isEmpty());
        row.put("cycle_length", settings.cycleLength());
        row.put("until_reset", Durations.format(service.clock().untilReset()));
        if (state == null) {
            row.putAll(Map.of("streak", 0, "longest", 0, "total", 0, "freezes", 0, "claimed_today", false,
                    "unclaimed", 0, "can_claim", false, "at_risk", false, "broken", false, "lost", 0));
            row.put("cycle_day", 1);
            row.put("next_milestone", 0);
            row.put("next_milestone_in", -1);
            return List.of(row);
        }
        StreakRules.Displayed shown = service.displayed(state);
        int todayStreak = shown.countedToday() ? shown.streak() : shown.streak() + 1;
        row.put("streak", shown.streak());
        row.put("longest", state.longest());
        row.put("total", state.totalDays());
        row.put("freezes", state.freezes());
        int unclaimed = service.unclaimed(player.getUniqueId());
        row.put("claimed_today", shown.countedToday() && !service.unclaimedOn(player.getUniqueId(), service.clock().today()));
        row.put("unclaimed", unclaimed);
        row.put("can_claim", unclaimed > 0 || !shown.countedToday());
        row.put("at_risk", shown.atRisk());
        row.put("broken", shown.broken());
        row.put("lost", shown.broken() ? state.streak() : 0);
        row.put("cycle_day", StreakRules.cycleDay(todayStreak, settings.cycleLength()));
        List<DailyService.UpcomingMilestone> next = service.upcomingMilestones(player.getUniqueId(), 1);
        boolean any = next != null && !next.isEmpty();
        row.put("next_milestone", any ? next.get(0).streak() : 0);
        row.put("next_milestone_in", any ? next.get(0).offset() : -1);
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
