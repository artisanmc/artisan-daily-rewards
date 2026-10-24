package com.github.ibanetchep.artisandailyrewards;

import com.github.ibanetchep.artisandailyrewards.artisan.DailyRewardsModule;
import com.github.ibanetchep.artisandailyrewards.command.DailyAdminCommand;
import com.github.ibanetchep.artisandailyrewards.command.DailyCommand;
import com.github.ibanetchep.artisandailyrewards.core.GameClock;
import com.github.ibanetchep.artisandailyrewards.listener.PlayerConnectionListener;
import com.github.ibanetchep.artisandailyrewards.storage.DailyRepository;
import net.artisanmc.modules.api.ArtisanAPI;
import net.artisanmc.modules.api.database.DatabaseDialect;
import net.artisanmc.modules.api.translation.MessageBundleDeclaration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Level;

/**
 * Entry point. Artisan is required: its database keeps the streaks, its action engine pays the
 * rewards, its editor edits the calendar. Without a compatible Artisan the plugin disables itself
 * and says why.
 */
public final class DailyRewardsPlugin extends JavaPlugin {

    /**
     * Lowest Artisan API this plugin compiles against: messages in the module's folder (2.17.0),
     * {@code MessageBundle.send} (2.10.0),
     * {@code DatabaseApi.tablePrefix} (2.9.0),
     * {@code ProjectRootRegistry.moduleFolder} (2.8.0), Config screens with a module view and {@code PlayerCommandRegistry} (2.7.0),
     * {@code TemplateRegistry} (2.6.0), {@code TranslationsApi} (2.5.0).
     */
    public static final String MIN_API_VERSION = "2.17.0";

    private @Nullable ArtisanAPI api;
    private @Nullable DailyService service;

    @Override
    public void onEnable() {
        api = resolveArtisan();
        if (api == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        DailyRepository repository;
        try {
            repository = new DailyRepository(api.getDatabase().dataSource(this),
                    api.getDatabase().tablePrefix(DailyRepository.MODULE));
            repository.migrate();
        } catch (SQLException | IllegalStateException | IllegalArgumentException e) {
            getLogger().log(Level.SEVERE, "The database is not available, ArtisanDailyRewards is disabled. "
                    + "Check plugins/Artisan/environment.yml.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Messages messages = new Messages(api.getTranslations().register(
                messageFiles(api)));
        // H2 is a file of this server; MySQL/MariaDB may be shared by the network.
        boolean shared = api.getDatabase().getDialect() != DatabaseDialect.H2;
        service = new DailyService(api.getScheduler().forPlugin(this), repository, messages, getLogger(),
                GameClock.system(LocalTime.MIDNIGHT), serverName(), shared);
        try {
            service.checkZone();
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, "Could not check the time zone recorded for the daily rewards", e);
        }

        api.getModules().register(new DailyRewardsModule(service, List.of(
                new DailyCommand(service, messages).declaration(),
                new DailyAdminCommand(service, messages, getLogger()).declaration()), getLogger()));
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(service), this);

        // After a reload of the plugin, players already online are loaded (and greeted) as if they joined.
        for (Player player : getServer().getOnlinePlayers()) service.join(player);
        getLogger().info("Enabled in " + service.clock().zone().getId() + " (Artisan API " + api.getApiVersion() + ").");
    }

    /** Written on each claim, to tell servers apart in the history: {@code host:port}. */
    private String serverName() {
        String ip = getServer().getIp();
        return (ip == null || ip.isBlank() ? "*" : ip) + ":" + getServer().getPort();
    }

    /**
     * The messages, read from {@code plugins/Artisan/modules/daily/lang/} beside the module's
     * other files: Artisan moves {@code plugins/ArtisanDailyRewards/lang/*.yml} there once. Without that
     * folder they stay in this plugin's.
     */
    private MessageBundleDeclaration messageFiles(ArtisanAPI api) {
        MessageBundleDeclaration files = MessageBundleDeclaration.files(this, Messages.NAMESPACE, "en", "fr");
        try {
            return files.inFolder(api.getProjectRoots().moduleFolder(DailyRewardsModule.FOLDER, DailyRewardsModule.ID, null));
        } catch (RuntimeException e) {
            getLogger().log(Level.WARNING, "No folder plugins/Artisan/modules/" + DailyRewardsModule.FOLDER
                    + ": the messages stay in plugins/" + getName() + "/lang/", e);
            return files;
        }
    }

    @Override
    public void onDisable() {
        if (api != null) {
            api.getModules().unregister(DailyRewardsModule.ID);
        }
        if (service != null) service.stop();
        service = null;
        api = null;
    }

    private @Nullable ArtisanAPI resolveArtisan() {
        try {
            ArtisanAPI found = getServer().getServicesManager().load(ArtisanAPI.class);
            if (found == null) {
                getLogger().severe("Artisan is required but its API is not available. "
                        + "Install Artisan (API " + MIN_API_VERSION + "+) next to ArtisanDailyRewards.");
                return null;
            }
            if (!found.isCompatibleWith(MIN_API_VERSION)) {
                getLogger().severe("Artisan API " + found.getApiVersion() + " is too old: ArtisanDailyRewards needs "
                        + MIN_API_VERSION + "+ (same major). Update Artisan.");
                return null;
            }
            return found;
        } catch (LinkageError e) {
            getLogger().log(Level.SEVERE, "Installed Artisan is incompatible: ArtisanDailyRewards needs API "
                    + MIN_API_VERSION + "+.", e);
            return null;
        }
    }
}
