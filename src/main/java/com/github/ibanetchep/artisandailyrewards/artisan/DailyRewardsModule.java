package com.github.ibanetchep.artisandailyrewards.artisan;

import com.github.ibanetchep.artisandailyrewards.DailyService;
import net.artisanmc.modules.api.ArtisanAPI;
import net.artisanmc.modules.api.ArtisanModule;
import net.artisanmc.modules.api.playercommand.PlayerCommandDeclaration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ArtisanDailyRewards inside Artisan:
 * <ul>
 *   <li>the module's folder {@code plugins/Artisan/modules/daily/}, whose {@code data/calendar.yaml}
 *       holds the settings and the rewards, edited in the editor's Calendar screen
 *       ({@link CalendarModel});</li>
 *   <li>its system files ({@link ModuleFiles}), kept there by Artisan: the calendar menu
 *       {@code daily:calendar}, which {@code /daily} opens, the calendar and its model;</li>
 *   <li>the data sources {@code daily:calendar} and {@code daily:status} of that menu
 *       ({@link CalendarData});</li>
 *   <li>the {@code /daily} and {@code /dailyadmin} commands; the menu's claim button runs
 *       {@code daily:claim} through {@code module_command}.</li>
 * </ul>
 * The {@code daily:*} actions and the placeholders come with the next steps of the design.
 */
public final class DailyRewardsModule implements ArtisanModule {

    public static final String ID = "daily";
    public static final String FOLDER = "daily";

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
        registerFiles(api);
        registerCalendarScreen(api);
        CalendarData data = new CalendarData(service);
        api.getDataSources().register(data.calendar());
        api.getDataSources().register(data.status());
        // A first start reads the calendar before Artisan writes the shipped one (off the game
        // thread): it runs on the defaults until the next reload.
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

    /**
     * The menu, the calendar and its model. The module file ({@code artisan-module.yml}: the
     * folder's languages) cannot be shipped in a template: written when the folder has none.
     */
    private void registerFiles(ArtisanAPI api) {
        if (folder != null) {
            try {
                if (ModuleFiles.writeModuleFile(folder)) logger.fine("Wrote the module file of " + folder.getPath());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not write the module file of " + folder.getPath(), e);
            }
        }
        try {
            api.getTemplates().register(ModuleFiles.template(getClass().getClassLoader()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.log(Level.WARNING, "The calendar menu and the shipped calendar are not available", e);
        }
    }

    /** The form of data/calendar.yaml: the Calendar screen of the daily folder. */
    private void registerCalendarScreen(ArtisanAPI api) {
        try {
            api.getModels().declare(CalendarModel.declaration());
            api.getScreens().declare(CalendarModel.SCREEN, "Calendar", CalendarModel.TABLE, FOLDER);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "The calendar screen is not available", e);
        }
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
