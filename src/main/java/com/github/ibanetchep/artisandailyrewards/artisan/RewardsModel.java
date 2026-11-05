package com.github.ibanetchep.artisandailyrewards.artisan;

import com.github.ibanetchep.artisandailyrewards.core.CalendarSettings;
import net.artisanmc.modules.api.model.ModelDeclaration;
import net.artisanmc.modules.api.model.ModelField;
import net.artisanmc.modules.api.model.ModelFieldType;

import java.util.List;

/**
 * {@code daily:rewards} (model/v1): the shape of {@code data/rewards.yml}, the cycle and the
 * reward of each of its days. The Rewards screen shows it as a grid of the cycle, the view
 * {@link #COMPONENT} of the module's editor bundle ({@code editor/}); an editor without that
 * bundle falls back to the form this model generates. ModuleFilesTest keeps the keys in step
 * with {@link CalendarSettings#read}.
 */
public final class RewardsModel {

    public static final String ID = "daily:rewards";
    public static final String SCREEN = "daily:rewards";
    /** The table the screen edits: the rewards without their extension. */
    public static final String TABLE = CalendarFiles.REWARDS;
    /** The key of the grid view in the bundle's {@code FrontendModule.screens}. */
    public static final String COMPONENT = "rewards";

    private RewardsModel() {
    }

    public static ModelDeclaration declaration() {
        CalendarSettings d = CalendarSettings.DEFAULTS;
        return new ModelDeclaration(ID, "id", List.of(
                SettingsModel.integer("cycle_length", "Cycle length", "Days before the rewards start again at day 1.", d.cycleLength(), 1),
                new ModelField("days", ModelFieldType.map(reward())).withLabel("Rewards by day")
                        .withHelp("Keyed by the day of the cycle, 1 to the cycle length. The icon is what the "
                                + "calendar menu shows, the actions what a claim of that day runs ({streak}, "
                                + "{cycle_day}, {cycle_length})."),
                new ModelField("default_day", reward()).withLabel("Reward of the other days")
                        .withHelp("For a day the list above does not hold. Empty: no reward that day.")))
                .withName("Daily rewards");
    }

    private static ModelFieldType reward() {
        return ModelFieldType.object(List.of(
                new ModelField("icon", ModelFieldType.item()).withLabel("Icon")
                        .withHelp("Shown in the calendar menu: name it after the reward."),
                new ModelField("actions", ModelFieldType.actions()).withLabel("Actions")
                        .withHelp("Run when the day is claimed: give_item, give_money, a command…")));
    }
}
