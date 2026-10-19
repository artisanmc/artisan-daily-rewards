package com.github.ibanetchep.artisandailyrewards.artisan;

import net.artisanmc.modules.api.ArtisanAPI;
import net.artisanmc.modules.api.ArtisanModule;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ArtisanDailyRewards inside Artisan. For now, the module's folder
 * {@code plugins/Artisan/modules/daily/}, listed under the module in the editor. The calendar
 * screen, the {@code daily:*} actions, the commands, the placeholders and the shipped template
 * come with the next steps of the design.
 */
public final class DailyRewardsModule implements ArtisanModule {

    public static final String ID = "daily";
    static final String FOLDER = "daily";

    private final Logger logger;
    private @Nullable File folder;

    public DailyRewardsModule(Logger logger) {
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
    }

    @Override
    public void onDisable() {
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
