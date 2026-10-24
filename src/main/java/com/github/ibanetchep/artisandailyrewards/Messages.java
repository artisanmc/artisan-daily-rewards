package com.github.ibanetchep.artisandailyrewards;

import net.artisanmc.modules.api.translation.MessageBundle;
import org.bukkit.command.CommandSender;

import java.util.Map;

/**
 * The plugin's system messages, served by Artisan's translations (namespace {@code daily}):
 * {@code lang/<code>.yml} in the jar, overridden key by key by
 * {@code plugins/Artisan/modules/daily/lang/<code>.yml} (the module's folder).
 */
public final class Messages {

    public static final String NAMESPACE = "daily";

    private final MessageBundle bundle;

    public Messages(MessageBundle bundle) {
        this.bundle = bundle;
    }

    public void send(CommandSender to, String key) {
        bundle.send(to, key);
    }

    /** Placeholder values are inserted as plain text: a player name cannot inject MiniMessage tags. */
    public void send(CommandSender to, String key, Map<String, ?> placeholders) {
        bundle.send(to, key, placeholders);
    }

    public void reload() {
        bundle.reload();
    }
}
