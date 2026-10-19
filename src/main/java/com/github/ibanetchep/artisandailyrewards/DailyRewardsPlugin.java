package com.github.ibanetchep.artisandailyrewards;

import com.github.ibanetchep.artisandailyrewards.artisan.DailyRewardsModule;
import net.artisanmc.modules.api.ArtisanAPI;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Entry point. Artisan is required: its database keeps the streaks, its action engine pays the
 * rewards, its editor edits the calendar. Without a compatible Artisan the plugin disables itself
 * and says why.
 */
public final class DailyRewardsPlugin extends JavaPlugin {

    /**
     * Lowest Artisan API this plugin compiles against: {@code ProjectRootRegistry.moduleFolder}
     * (2.8.0), Config screens with a module view and {@code PlayerCommandRegistry} (2.7.0),
     * {@code TemplateRegistry} (2.6.0), {@code TranslationsApi} (2.5.0).
     */
    public static final String MIN_API_VERSION = "2.8.0";

    private @Nullable ArtisanAPI api;

    @Override
    public void onEnable() {
        api = resolveArtisan();
        if (api == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        api.getModules().register(new DailyRewardsModule(getLogger()));
        getLogger().info("Enabled (Artisan API " + api.getApiVersion() + ").");
    }

    @Override
    public void onDisable() {
        if (api != null) {
            api.getModules().unregister(DailyRewardsModule.ID);
        }
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
