package com.github.ibanetchep.artisandailyrewards.artisan;

import com.github.ibanetchep.artisandailyrewards.core.CalendarSettings;
import net.artisanmc.modules.api.model.ModelDeclaration;
import net.artisanmc.modules.api.model.ModelField;
import net.artisanmc.modules.api.model.ModelFieldType;

import java.util.List;

/**
 * {@code daily:calendar} (model/v1): the shape of {@code data/calendar.yaml}, declared to the
 * editor so the Calendar screen of the daily folder renders a form. The field names are the
 * keys {@link CalendarSettings#read} reads; CalendarModelTest keeps both in step.
 */
public final class CalendarModel {

    public static final String ID = "daily:calendar";
    public static final String SCREEN = "daily:calendar";
    /** The table the screen edits: the calendar without its extension. */
    public static final String TABLE = "data/calendar";

    private CalendarModel() {
    }

    public static ModelDeclaration declaration() {
        CalendarSettings d = CalendarSettings.DEFAULTS;
        return new ModelDeclaration(ID, "id", List.of(
                new ModelField("reset_time", ModelFieldType.string()).withLabel("Reset time")
                        .withHelp("When the game day ends, server time: \"00:00\", \"06:00\".")
                        .withPattern("^([01]\\d|2[0-3]):[0-5]\\d$").withDefault("00:00"),
                integer("cycle_length", "Cycle length", "Days before the rewards start again at day 1.", d.cycleLength(), 1),
                new ModelField("auto_claim", ModelFieldType.enumOf(List.of("off", "on", "player"))).withLabel("Auto-claim")
                        .withHelp("off: the player claims. on: claimed at join and at the reset. player: each player "
                                + "chooses with /daily autoclaim.")
                        .withDefault("off"),
                integer("min_playtime_minutes", "Playtime before the day counts",
                        "Minutes of the current session before the day counts for the streak and its reward can be claimed.",
                        d.minPlaytimeMinutes(), 0),
                new ModelField("join_reminder", ModelFieldType.bool()).withLabel("Remind at join")
                        .withHelp("Tell a player their reward is unlocked when the day counts, and remind of the rewards that wait.").withDefault(d.joinReminder()),
                new ModelField("freeze", ModelFieldType.object(List.of(
                        integer("earn_every", "Earned every", "Days of streak per joker; 0: never.", d.freezeEarnEvery(), 0),
                        integer("max_stock", "Held at most", null, d.freezeMaxStock(), 0))))
                        .withLabel("Jokers")
                        .withHelp("A joker covers a missed day: the streak goes on."),
                new ModelField("days", ModelFieldType.map(reward())).withLabel("Rewards by day")
                        .withHelp("Keyed by the day of the cycle, 1 to the cycle length. The icon is what the "
                                + "calendar menu shows, the actions what a claim of that day runs ({streak}, "
                                + "{cycle_day}, {cycle_length})."),
                new ModelField("default_day", reward()).withLabel("Reward of the other days")
                        .withHelp("For a day the list above does not hold. Empty: no reward that day.")))
                .withName("Daily rewards calendar");
    }

    private static ModelFieldType reward() {
        return ModelFieldType.object(List.of(
                new ModelField("icon", ModelFieldType.item()).withLabel("Icon")
                        .withHelp("Shown in the calendar menu: name it after the reward."),
                new ModelField("actions", ModelFieldType.actions()).withLabel("Actions")
                        .withHelp("Run when the day is claimed: give_item, give_money, a command…")));
    }

    private static ModelField integer(String name, String label, String help, int defaultValue, int min) {
        return new ModelField(name, ModelFieldType.integer()).withLabel(label).withHelp(help)
                .withDefault(defaultValue).withRange((double) min, null);
    }
}
