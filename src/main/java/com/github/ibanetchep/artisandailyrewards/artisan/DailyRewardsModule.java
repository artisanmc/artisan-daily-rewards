package com.github.ibanetchep.artisandailyrewards.artisan;

import com.github.ibanetchep.artisandailyrewards.DailyService;
import net.artisanmc.modules.api.ArtisanAPI;
import net.artisanmc.modules.api.ArtisanModule;
import net.artisanmc.modules.api.playercommand.PlayerCommandDeclaration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ArtisanDailyRewards inside Artisan: the module's folder {@code plugins/Artisan/modules/daily/}
 * (its {@code data/calendar.yaml} holds the settings) and the {@code /daily} and
 * {@code /dailyadmin} commands. The calendar screen, the {@code daily:*} actions, the placeholders
 * and the shipped template come with the next steps of the design.
 */
public final class DailyRewardsModule implements ArtisanModule {

    public static final String ID = "daily";
    static final String FOLDER = "daily";

    private final DailyService service;
    private final List<PlayerCommandDeclaration> commands;
    private final Logger logger;
    private @Nullable File folder;

    public DailyRewardsModule(DailyService service, List<PlayerCommandDeclaration> commands, Logger logger) {
        this.service = service;
        this.commands = commands;
        this.logger = logger;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getSchemaVersion() {
        return "daily/v1";
    }

    @Override
    public void onEnable(ArtisanAPI api) {
        claimFolder(api);
        service.loadCalendar(folder);
        // Owned by this module: Artisan removes the commands when the module is unregistered.
        commands.forEach(api.getPlayerCommands()::register);
    }

    /** {@code /artisan reload}: the calendar is read again; on an error the previous settings stay. */
    @Override
    public void onReload() {
        service.loadCalendar(folder);
    }

    @Override
    public void onDisable() {
        service.stop();
        folder = null;
    }

    /** {@code modules/daily/}, before any template: Artisan seeds the shipped files there. */
    private void claimFolder(ArtisanAPI api) {
        try {
            folder = api.getProjectRoots().moduleFolder(FOLDER, ID, null);
            logger.fine("Module folder: " + folder.getPath());
        } catch (IllegalArgumentException | IllegalStateException | UnsupportedOperationException e) {
            logger.log(Level.WARNING, "The folder plugins/Artisan/modules/" + FOLDER + " is not available", e);
        }
    }
}
