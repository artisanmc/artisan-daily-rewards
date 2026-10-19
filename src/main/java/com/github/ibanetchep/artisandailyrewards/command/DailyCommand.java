package com.github.ibanetchep.artisandailyrewards.command;

import com.github.ibanetchep.artisandailyrewards.DailyService;
import com.github.ibanetchep.artisandailyrewards.Messages;
import com.github.ibanetchep.artisandailyrewards.core.Durations;
import com.github.ibanetchep.artisandailyrewards.core.PlayerState;
import com.github.ibanetchep.artisandailyrewards.core.StreakRules;
import net.artisanmc.modules.api.playercommand.CommandArgument;
import net.artisanmc.modules.api.playercommand.PlayerCommandContext;
import net.artisanmc.modules.api.playercommand.PlayerCommandDeclaration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * {@code /daily}, declared to Artisan ({@code api.getPlayerCommands()}): the player's streak, and
 * {@code claim} and {@code autoclaim [on|off]}. Artisan checks the permission, parses and
 * completes the arguments; the admin renames the command, sets its aliases or a cooldown with a
 * {@code command_override/v1} targeting {@link #ID}.
 */
public final class DailyCommand {

    /** What an override targets; never changes. */
    public static final String ID = "daily:daily";
    public static final String NAME = "daily";
    public static final String PERMISSION = "daily.use";

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
                .executor(this::status)
                .child(PlayerCommandDeclaration.builder("daily:claim")
                        .name("claim")
                        .permission(PERMISSION)
                        .description(Map.of("en", "Claim today's reward", "fr", "Réclame la récompense du jour"))
                        .executor(ctx -> withPlayer(ctx, player -> service.claim(player, false)))
                        .build())
                .child(PlayerCommandDeclaration.builder("daily:autoclaim")
                        .name("autoclaim")
                        .permission(PERMISSION)
                        .argument(CommandArgument.choice("state", List.of("on", "off")).optional())
                        .description(Map.of(
                                "en", "Claim automatically when you join",
                                "fr", "Réclame automatiquement à la connexion"))
                        .executor(ctx -> withPlayer(ctx, player -> {
                            String state = ctx.get("state", String.class);
                            service.setAutoClaim(player, state == null ? null : state.equals("on"));
                        }))
                        .build())
                .build();
    }

    /** The menu comes with the Artisan integration; until then, the streak in chat. */
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
                    "freezes", state.freezes()));
            if (shown.claimedToday()) {
                messages.send(player, "status.claimed", Map.of("time", Durations.format(service.clock().untilReset())));
            } else {
                messages.send(player, "status.waiting");
            }
            if (shown.atRisk()) messages.send(player, "status.at_risk");
            if (shown.broken()) messages.send(player, "status.broken", Map.of("lost", state.streak()));
        });
    }

    /** Artisan refuses the console on these commands: they are not {@code consoleAllowed}. */
    private static void withPlayer(PlayerCommandContext ctx, java.util.function.Consumer<Player> action) {
        Player player = ctx.getPlayer();
        if (player != null) action.accept(player);
    }
}
