package com.github.ibanetchep.artisandailyrewards.artisan;

import com.github.ibanetchep.artisandailyrewards.core.CalendarSettings;
import net.artisanmc.modules.api.model.ModelDeclaration;
import net.artisanmc.modules.api.model.ModelField;
import net.artisanmc.modules.api.model.ModelFieldType;

import java.util.List;

/**
 * {@code daily:settings} (model/v1): the shape of {@code data/settings.yml}, declared to the
 * editor so the Settings screen of the daily folder renders a form. The field names are keys
 * {@link CalendarSettings#read} reads; ModuleFilesTest keeps both in step. The rewards and the
 * cycle are in {@link RewardsModel}.
 */
public final class SettingsModel {

    public static final String ID = "daily:settings";
    public static final String SCREEN = "daily:settings";
    /** The table the screen edits: the settings without their extension. */
    public static final String TABLE = CalendarFiles.SETTINGS;

    private SettingsModel() {
    }

    public static ModelDeclaration declaration() {
        CalendarSettings d = CalendarSettings.DEFAULTS;
        return new ModelDeclaration(ID, "id", List.of(
                new ModelField("reset_time", ModelFieldType.string()).withLabel("Reset time")
                        .withHelp("When the game day ends, server time: \"00:00\", \"06:00\".")
                        .withPattern("^([01]\\d|2[0-3]):[0-5]\\d$").withDefault("00:00"),
                new ModelField("auto_claim", ModelFieldType.enumOf(List.of("off", "on", "player"))).withLabel("Auto-claim")
                        .withHelp("off: the player claims. on: claimed at join and at the reset. player: each player "
                                + "chooses with /dailyrewards autoclaim.")
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
                        .withHelp("A joker covers a missed day: the streak goes on.")))
                .withName("Daily rewards settings");
    }

    static ModelField integer(String name, String label, String help, int defaultValue, int min) {
        return new ModelField(name, ModelFieldType.integer()).withLabel(label).withHelp(help)
                .withDefault(defaultValue).withRange((double) min, null);
    }
}
