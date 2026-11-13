package com.github.ibanetchep.artisandailyrewards.artisan;

import com.github.ibanetchep.artisandailyrewards.DailyService;
import net.artisanmc.modules.api.ArtisanAPI;
import net.artisanmc.modules.api.ArtisanModule;
import net.artisanmc.modules.api.editor.EditorBundleDeclaration;
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
 *   <li>the module's folder {@code plugins/ArtisanDailyRewards/artisan_module/}, whose {@code data/settings.yml}
 *       and {@code data/rewards.yml} ({@link CalendarFiles}) are edited in the editor's Settings
 *       screen ({@link SettingsModel}, a generated form) and Rewards screen ({@link RewardsModel},
 *       a grid of the cycle from the module's editor bundle, {@link #EDITOR_BUNDLE});</li>
 *   <li>its system files ({@link ModuleFiles}), kept there by Artisan: the calendar menu
 *       {@code daily:calendar}, which {@code /daily} opens, the two files and their models;</li>
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
    /** The folder of the module's files inside the plugin's data folder. */
    public static final String MODULE_DIR = "artisan_module";
    /** The editor view of the module (the Rewards grid), built from {@code editor/} into the jar. */
    static final String EDITOR_BUNDLE = "artisan-editor/ui.js";
    /** The {@code @artisan/editor-sdk} version the bundle is built against: keep it with editor/package.json. */
    static final String EDITOR_SDK = "1.20";

    private final DailyService service;
    private final List<PlayerCommandDeclaration> commands;
    private final Logger logger;
    /** Where the old {@code data/calendar.yml} is copied when it is split: the plugin's own folder. */
    private final File backupDir;
    private final File moduleDir;
    private @Nullable File folder;

    public DailyRewardsModule(DailyService service, List<PlayerCommandDeclaration> commands, Logger logger, File backupDir, File moduleDir) {
        this.moduleDir = moduleDir;
        this.service = service;
        this.commands = commands;
        this.logger = logger;
        this.backupDir = backupDir;
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
        migrateCalendar();
        registerFiles(api);
        registerScreens(api);
        registerEditorBundle(api);
        CalendarData data = new CalendarData(service);
        api.getDataSources().register(data.calendar());
        api.getDataSources().register(data.status());
        // A first start reads the calendar before Artisan writes the shipped files (off the game
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
     * The menu, the settings, the rewards and their models. The module file ({@code artisan-module.yml}: the
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
            logger.log(Level.WARNING, "The calendar menu and the shipped settings and rewards are not available", e);
        }
    }

    /**
     * The Settings screen (a generated form) and the Rewards screen (the bundle's grid, else the
     * form of its model) of the daily folder.
     */
    private void registerScreens(ArtisanAPI api) {
        try {
            api.getModels().declare(SettingsModel.declaration());
            api.getModels().declare(RewardsModel.declaration());
            api.getScreens().declare(SettingsModel.SCREEN, "Settings", SettingsModel.TABLE, FOLDER);
            api.getScreens().declare(RewardsModel.SCREEN, "Rewards", RewardsModel.TABLE, FOLDER, RewardsModel.COMPONENT);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "The settings and rewards screens are not available", e);
        }
    }

    /** The module's editor view; without it the Rewards screen shows its generated form. */
    private void registerEditorBundle(ArtisanAPI api) {
        try {
            api.getEditorBundles().register(
                    EditorBundleDeclaration.fromResource(getClass().getClassLoader(), EDITOR_BUNDLE, EDITOR_SDK));
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "The rewards grid is not available in the editor: its form is shown instead", e);
        }
    }

    /**
     * Before the shipped files: an edited {@code data/calendar.yml} of an earlier version is split
     * into the settings and the rewards, so Artisan keeps both as the owner's edits.
     */
    private void migrateCalendar() {
        if (folder == null) return;
        try {
            switch (CalendarFiles.migrate(folder, backupDir)) {
                case SPLIT -> logger.info("Split data/calendar.yml into data/settings.yml and data/rewards.yml "
                        + "(edited in the Settings and Rewards screens); the old file is kept in " + backupDir.getPath());
                case LEFT -> logger.warning("data/calendar.yml is no longer read: data/settings.yml and "
                        + "data/rewards.yml hold the calendar. Remove it once its values are there.");
                case NONE -> { }
            }
        } catch (IOException | RuntimeException e) {
            logger.log(Level.SEVERE, "Could not split data/calendar.yml into data/settings.yml and data/rewards.yml", e);
        }
    }

    /** {@code plugins/ArtisanDailyRewards/artisan_module/}, before any template: Artisan seeds the shipped files there. */
    private void claimFolder(ArtisanAPI api) {
        try {
            folder = api.getProjectRoots().pluginFolder(FOLDER, ID, moduleDir);
            logger.fine("Module folder: " + folder.getPath());
        } catch (IllegalArgumentException | IllegalStateException | UnsupportedOperationException e) {
            logger.log(Level.WARNING, "The folder " + moduleDir.getPath() + " is not available", e);
        }
    }
}
