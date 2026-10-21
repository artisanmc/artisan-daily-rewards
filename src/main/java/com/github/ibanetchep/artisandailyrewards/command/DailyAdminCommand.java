package com.github.ibanetchep.artisandailyrewards.command;

import com.github.ibanetchep.artisandailyrewards.DailyService;
import com.github.ibanetchep.artisandailyrewards.Messages;
import com.github.ibanetchep.artisandailyrewards.core.PlayerState;
import com.github.ibanetchep.artisandailyrewards.core.StreakRules;
import com.github.ibanetchep.artisandailyrewards.storage.DailyRepository;
import com.github.ibanetchep.artisandailyrewards.storage.LedgerEntry;
import net.artisanmc.modules.api.playercommand.CommandArgument;
import net.artisanmc.modules.api.playercommand.PlayerCommandContext;
import net.artisanmc.modules.api.playercommand.PlayerCommandDeclaration;
import net.artisanmc.modules.api.playercommand.PlayerCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code /dailyadmin} ({@code daily.admin}): {@code info <player>}, {@code set <player>
 * streak|longest|freezes <n>} and {@code day [+n|-n|reset]}. The console may use it.
 */
public final class DailyAdminCommand {

    public static final String ID = "daily:admin";
    public static final String NAME = "dailyadmin";
    public static final String PERMISSION = "daily.admin";

    private static final DateTimeFormatter LEDGER_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DailyService service;
    private final Messages messages;
    private final Logger logger;

    public DailyAdminCommand(DailyService service, Messages messages, Logger logger) {
        this.service = service;
        this.messages = messages;
        this.logger = logger;
    }

    public PlayerCommandDeclaration declaration() {
        return PlayerCommandDeclaration.builder(ID)
                .name(NAME)
                .permission(PERMISSION)
                .consoleAllowed(true)
                .description(Map.of(
                        "en", "Inspect and correct daily reward streaks",
                        "fr", "Inspecte et corrige les streaks de récompenses quotidiennes"))
                .child(child("daily:admin_info", "info", this::info)
                        .argument(CommandArgument.string("player"))
                        .description(Map.of("en", "A player's streak and ledger", "fr", "Streak et journal d'un joueur"))
                        .build())
                .child(child("daily:admin_set", "set", this::set)
                        .argument(CommandArgument.string("player"))
                        .argument(CommandArgument.choice("field", List.of("streak", "longest", "freezes")))
                        .argument(CommandArgument.integer("value"))
                        .description(Map.of("en", "Correct a counter", "fr", "Corrige un compteur"))
                        .build())
                .child(child("daily:admin_day", "day", this::day)
                        .argument(CommandArgument.string("shift").optional())
                        .description(Map.of(
                                "en", "Shift this server's game day to test (+n, -n, reset)",
                                "fr", "Décale le jour de jeu de ce serveur pour tester (+n, -n, reset)"))
                        .build())
                .build();
    }

    private static PlayerCommandDeclaration.Builder child(String id, String name, PlayerCommandExecutor executor) {
        return PlayerCommandDeclaration.builder(id)
                .name(name)
                .permission(PERMISSION)
                .consoleAllowed(true)
                .executor(executor);
    }

    private void info(PlayerCommandContext ctx) {
        CommandSender sender = ctx.getSender();
        String name = ctx.get("player", String.class);
        UUID uuid = resolve(sender, name);
        if (uuid == null) return;
        service.info(uuid).whenComplete((info, error) -> {
            if (error != null) {
                logger.log(Level.SEVERE, "Could not read the streak of " + name, error);
                messages.send(sender, "command.error");
                return;
            }
            PlayerState s = info.state();
            StreakRules.Displayed shown = service.displayed(s);
            messages.send(sender, "admin.info.header", Map.of("player", name));
            messages.send(sender, "admin.info.state", Map.of(
                    "streak", s.streak(),
                    "shown", shown.streak(),
                    "longest", s.longest(),
                    "total", s.totalClaims(),
                    "freezes", s.freezes(),
                    "last", s.lastDay() != null ? LocalDate.ofEpochDay(s.lastDay()).toString() : "-"));
            if (s.brokenStreak() > 0 && s.brokenFromDay() != null) {
                messages.send(sender, "admin.info.broken", Map.of(
                        "lost", s.brokenStreak(),
                        "from", LocalDate.ofEpochDay(s.brokenFromDay()).toString()));
            }
            ZoneId zone = service.clock().zone();
            for (LedgerEntry line : info.ledger()) {
                messages.send(sender, "admin.info.ledger", Map.of(
                        "at", LEDGER_TIME.format(Instant.ofEpochMilli(line.at()).atZone(zone)),
                        "kind", line.kind().column(),
                        "amount", line.amount(),
                        "detail", line.detail() != null ? line.detail() : ""));
            }
        });
    }

    private void set(PlayerCommandContext ctx) {
        CommandSender sender = ctx.getSender();
        String name = ctx.get("player", String.class);
        String field = ctx.get("field", String.class);
        Integer value = ctx.get("value", Integer.class);
        UUID uuid = resolve(sender, name);
        if (uuid == null || field == null || value == null) return;
        if (value < 0) {
            messages.send(sender, "admin.set_negative");
            return;
        }
        DailyRepository.AdminField target = DailyRepository.AdminField.valueOf(field.toUpperCase(Locale.ROOT));
        service.adminSet(uuid, target, value, sender.getName()).whenComplete((state, error) -> {
            if (error != null) {
                logger.log(Level.SEVERE, "Could not correct the streak of " + name, error);
                messages.send(sender, "command.error");
                return;
            }
            messages.send(sender, "admin.set", Map.of("player", name, "field", field, "value", value));
        });
    }

    private void day(PlayerCommandContext ctx) {
        CommandSender sender = ctx.getSender();
        if (service.sharedDatabase()) {
            messages.send(sender, "admin.day_shared");
            return;
        }
        String shift = ctx.get("shift", String.class);
        if (shift != null) {
            if (shift.equalsIgnoreCase("reset")) {
                service.shiftDay(0);
            } else {
                long days;
                try {
                    days = Long.parseLong(shift.startsWith("+") ? shift.substring(1) : shift);
                } catch (NumberFormatException e) {
                    messages.send(sender, "admin.day_usage");
                    return;
                }
                service.shiftDay(days);
            }
        }
        long today = service.clock().today();
        messages.send(sender, "admin.day", Map.of(
                "date", LocalDate.ofEpochDay(today).toString(),
                "offset", service.clock().offsetDays()));
    }

    /** An online player, else one this server has seen (no lookup on Mojang's servers). */
    private @Nullable UUID resolve(CommandSender sender, @Nullable String name) {
        if (name == null) return null;
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online.getUniqueId();
        OfflinePlayer cached = cachedOfflinePlayer(name);
        if (cached != null) return cached.getUniqueId();
        messages.send(sender, "command.player_not_found", Map.of("player", name));
        return null;
    }

    /** Paper's cache of known players; Spigot lacks it, so there the known players are scanned. */
    private static @Nullable OfflinePlayer cachedOfflinePlayer(String name) {
        try {
            return Bukkit.getOfflinePlayerIfCached(name);
        } catch (NoSuchMethodError spigot) {
            for (OfflinePlayer known : Bukkit.getOfflinePlayers()) {
                if (name.equalsIgnoreCase(known.getName())) return known;
            }
            return null;
        }
    }
}
