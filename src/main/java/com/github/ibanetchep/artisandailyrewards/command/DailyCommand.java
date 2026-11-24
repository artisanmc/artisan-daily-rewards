package com.github.ibanetchep.artisandailyrewards.command;

import com.github.ibanetchep.artisandailyrewards.DailyService;
import com.github.ibanetchep.artisandailyrewards.Messages;
import com.github.ibanetchep.artisandailyrewards.core.Durations;
import com.github.ibanetchep.artisandailyrewards.core.PlayerState;
import com.github.ibanetchep.artisandailyrewards.core.StreakRules;
import net.artisanmc.modules.api.playercommand.CommandArgument;
import net.artisanmc.modules.api.playercommand.PlayerCommandContext;
import net.artisanmc.modules.api.playercommand.PlayerCommandDeclaration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.Duration;
import java.util.Locale;

import java.util.List;
import java.util.Map;

/**
 * {@code /dailyrewards}, declared to Artisan ({@code api.getPlayerCommands()}): opens the calendar menu
 * {@link #MENU}; {@code claim}, {@code autoclaim [on|off]}, and {@code status}, the streak in chat. Artisan checks the permission, parses and
 * completes the arguments. Artisan writes it as {@code commands/dailyrewards.yml} of the module's folder,
 * whose actions call {@link #ID} ({@code module_command}): the admin renames the command, sets its
 * aliases or a cooldown by editing that file.
 */
public final class DailyCommand {

    /** What the command file's {@code module_command} calls; never changes. */
    public static final String ID = "daily:daily";
    public static final String NAME = "dailyrewards";
    public static final String PERMISSION = "daily.use";
    /** The system menu of the module's folder {@code /dailyrewards} opens. */
    public static final String MENU = "daily:calendar";

    /** The name players type: the admin's rename in {@code commands/dailyrewards.yml} of the module's folder (Artisan names the file after {@link #NAME}), else {@link #NAME}. */
    public static String nameIn(File moduleDir) {
        File file = new File(moduleDir, "commands/" + NAME + ".yml");
        if (!file.isFile()) return NAME;
        String name = YamlConfiguration.loadConfiguration(file).getString("name");
        return name == null || name.isBlank() ? NAME : name;
    }

    private final DailyService service;
    private final Messages messages;

    public DailyCommand(DailyService service, Messages messages) {
        this.service = service;
        this.messages = messages;
    }

    public PlayerCommandDeclaration declaration() {
        return PlayerCommandDeclaration.builder(ID)
                .name(NAME)
                .permission(PERMISSION)
                .description(Map.of(
                        "en", "Your daily reward and streak",
                        "fr", "Ta récompense quotidienne et ton streak"))
                .opensMenu(MENU)
                .child(PlayerCommandDeclaration.builder("daily:status")
                        .name("status")
                        .permission(PERMISSION)
                        .description(Map.of("en", "Your streak, in chat", "fr", "Ton streak, dans le chat"))
                        .executor(this::status)
                        .build())
                .child(PlayerCommandDeclaration.builder("daily:claim")
                        .name("claim")
                        .permission(PERMISSION)
                        .description(Map.of("en", "Claim today's reward", "fr", "Réclame la récompense du jour"))
                        .executor(ctx -> withPlayer(ctx, player -> service.claim(player, false)))
                        .build())
                .child(PlayerCommandDeclaration.builder("daily:autoclaim")
                        .name("autoclaim")
                        .permission(PERMISSION)
                        // A word, not a choice: Artisan's YAML reads a plain `on`/`off` in the command
                        // file as a boolean, so a choice of on/off asked `true` of one parser and `on` of the other.
                        .argument(CommandArgument.string("state").optional().suggestions(ctx -> TOGGLE_SUGGESTIONS))
                        .description(Map.of(
                                "en", "Claim automatically when you join",
                                "fr", "Réclame automatiquement à la connexion"))
                        .executor(ctx -> withPlayer(ctx, player -> {
                            String state = ctx.get("state", String.class);
                            if (state == null || state.isBlank()) {
                                service.setAutoClaim(player, null);
                                return;
                            }
                            Boolean enabled = parseToggle(state);
                            if (enabled == null) messages.send(player, "autoclaim.invalid", Map.of("value", state.trim()));
                            else service.setAutoClaim(player, enabled);
                        }))
                        .build())
                .build();
    }

    /** {@code /dailyrewards status}: the streak in chat, what the menu's top item shows. */
    private void status(PlayerCommandContext ctx) {
        withPlayer(ctx, player -> {
            PlayerState state = service.state(player.getUniqueId());
            if (state == null) {
                messages.send(player, "status.loading");
                return;
            }
            StreakRules.Displayed shown = service.displayed(state);
            messages.send(player, "status.line", Map.of(
                    "streak", shown.streak(),
                    "longest", state.longest(),
                    "freezes", StreakRules.freezesShown(state, service.settings())));
            int waiting = service.unclaimed(player.getUniqueId());
            Duration lock = service.playtimeLock(player.getUniqueId());
            String key = claimKey(waiting, shown.countedToday(), !lock.isZero());
            if (key != null) messages.send(player, key, Map.of(
                    "count", waiting,
                    "time", Durations.format(service.clock().untilReset())));
            if (!lock.isZero()) messages.send(player, "status.playtime", Map.of("time", Durations.format(lock)));
            if (shown.atRisk()) messages.send(player, "status.at_risk");
            if (shown.broken()) messages.send(player, "status.broken", Map.of("lost", state.streak()));
        });
    }

    /**
     * What the status says of the rewards: {@code status.waiting_many} / {@code status.waiting} while
     * some wait to be claimed (today's too once it can be counted, a claim counting it), else
     * {@code status.claimed} when today is counted; null while today waits for its playtime and
     * nothing else waits ({@code status.playtime} says how long).
     */
    static String claimKey(int waiting, boolean countedToday, boolean playtimeLocked) {
        if (waiting > 1) return "status.waiting_many";
        if (waiting == 1 || (!countedToday && !playtimeLocked)) return "status.waiting";
        return countedToday ? "status.claimed" : null;
    }

    /** The values {@code autoclaim} completes; {@link #parseToggle} takes more. */
    static final List<String> TOGGLE_SUGGESTIONS = List.of("on", "off");

    /** {@code on}/{@code off}, {@code true}/{@code false}, {@code yes}/{@code no}, {@code oui}/{@code non}, ignoring case; null otherwise. */
    static Boolean parseToggle(String raw) {
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "on", "true", "yes", "oui" -> Boolean.TRUE;
            case "off", "false", "no", "non" -> Boolean.FALSE;
            default -> null;
        };
    }

    /** Artisan refuses the console on these commands: they are not {@code consoleAllowed}. */
    private static void withPlayer(PlayerCommandContext ctx, java.util.function.Consumer<Player> action) {
        Player player = ctx.getPlayer();
        if (player != null) action.accept(player);
    }
}
