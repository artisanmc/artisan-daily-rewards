package com.github.ibanetchep.artisandailyrewards;

import net.artisanmc.modules.api.translation.MessageBundle;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The plugin's system messages, served by Artisan's translations (namespace {@code daily}):
 * {@code lang/<code>.yml} in the jar, overridden key by key by
 * {@code plugins/ArtisanDailyRewards/artisan_module/lang/<code>.yml} (the module's folder).
 * Every message gets {@code <command>}, the calendar command's current name.
 */
public final class Messages {

    public static final String NAMESPACE = "daily";

    private final MessageBundle bundle;

    private final Supplier<String> command;

    public Messages(MessageBundle bundle, Supplier<String> command) {
        this.bundle = bundle;
        this.command = command;
    }

    public void send(CommandSender to, String key) {
        send(to, key, Map.of());
    }

    /** Placeholder values are inserted as plain text: a player name cannot inject MiniMessage tags. */
    public void send(CommandSender to, String key, Map<String, ?> placeholders) {
        Map<String, Object> values = new HashMap<>(placeholders);
        values.putIfAbsent("command", command.get());
        bundle.send(to, key, values);
    }

    public void reload() {
        bundle.reload();
    }
}
