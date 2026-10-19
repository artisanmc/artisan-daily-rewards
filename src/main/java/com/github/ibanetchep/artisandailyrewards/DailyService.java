package com.github.ibanetchep.artisandailyrewards;

import com.github.ibanetchep.artisandailyrewards.core.CalendarException;
import com.github.ibanetchep.artisandailyrewards.core.CalendarSettings;
import com.github.ibanetchep.artisandailyrewards.core.Durations;
import com.github.ibanetchep.artisandailyrewards.core.GameClock;
import com.github.ibanetchep.artisandailyrewards.core.PlayerState;
import com.github.ibanetchep.artisandailyrewards.core.StreakRules;
import com.github.ibanetchep.artisandailyrewards.storage.ClaimResult;
import com.github.ibanetchep.artisandailyrewards.storage.DailyRepository;
import com.github.ibanetchep.artisandailyrewards.storage.LedgerEntry;
import net.artisanmc.modules.api.scheduler.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The claim flow around the pure rule: the calendar settings, the cache of online players, the
 * claim itself (off the game threads, then back on the player's thread), auto-claim and the
 * reminder at join and at each reset.
 *
 * <p>The database is only touched from {@link TaskScheduler#runAsync}; the cache is read from
 * any thread and answers placeholders without I/O.
 */
public final class DailyService {

    /** Where the calendar lives in the module's folder. */
    public static final String CALENDAR_PATH = "data/calendar.yaml";

    private final TaskScheduler scheduler;
    private final DailyRepository repository;
    private final Messages messages;
    private final Logger logger;
    private final GameClock clock;
    private final String server;
    private final boolean sharedDatabase;

    private volatile CalendarSettings settings = CalendarSettings.DEFAULTS;
    /** False when this server's time zone differs from the network's: claiming would pay a day twice. */
    private volatile boolean claimsAllowed = true;

    private final Map<UUID, PlayerState> cache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> sessionStart = new ConcurrentHashMap<>();
    private final Set<UUID> claiming = ConcurrentHashMap.newKeySet();
    /** Raised on every reschedule: a reset task from an older schedule does nothing. */
    private final AtomicInteger resetGeneration = new AtomicInteger();

    public DailyService(TaskScheduler scheduler, DailyRepository repository, Messages messages, Logger logger,
                        GameClock clock, String server, boolean sharedDatabase) {
        this.scheduler = scheduler;
        this.repository = repository;
        this.messages = messages;
        this.logger = logger;
        this.clock = clock;
        this.server = server;
        this.sharedDatabase = sharedDatabase;
    }

    public CalendarSettings settings() {
        return settings;
    }

    public GameClock clock() {
        return clock;
    }

    public boolean sharedDatabase() {
        return sharedDatabase;
    }

    // ---- Calendar

    /**
     * Reads {@code data/calendar.yaml} of the module's folder. Missing: the defaults. Unreadable or
     * wrong: the previous settings stay and the error is logged. Reschedules the reset.
     */
    public void loadCalendar(@Nullable File moduleFolder) {
        File file = moduleFolder != null ? new File(moduleFolder, CALENDAR_PATH) : null;
        if (file == null || !file.isFile()) {
            apply(CalendarSettings.DEFAULTS);
            logger.info("No " + CALENDAR_PATH + " in the module folder yet: default settings.");
            return;
        }
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            Object root = new Yaml(new SafeConstructor(new LoaderOptions())).load(reader);
            apply(CalendarSettings.read(root instanceof Map<?, ?> map ? map : Map.of()));
        } catch (IOException | RuntimeException e) {
            logger.log(Level.SEVERE, "Cannot read " + file.getPath() + ", the previous settings stay", e);
        } catch (CalendarException e) {
            logger.severe(file.getPath() + " cannot be used, the previous settings stay: " + e.getMessage());
        }
    }

    private void apply(CalendarSettings next) {
        settings = next;
        clock.setResetTime(next.resetTime());
        scheduleReset();
    }

    /**
     * Records the server's time zone, or compares it with the one recorded. On a database of its
     * own a change is only logged; on a shared one, claims stop here until the zones agree.
     */
    public void checkZone() throws SQLException {
        String zone = clock.zone().getId();
        String recorded = repository.meta(DailyRepository.ZONE);
        if (recorded == null || recorded.equals(zone)) {
            if (recorded == null) repository.putMeta(DailyRepository.ZONE, zone);
            claimsAllowed = true;
            return;
        }
        if (!sharedDatabase) {
            logger.warning("The server's time zone changed from " + recorded + " to " + zone
                    + ": one game day may have been created or skipped for everyone.");
            repository.putMeta(DailyRepository.ZONE, zone);
            claimsAllowed = true;
            return;
        }
        claimsAllowed = false;
        logger.severe("This server runs in " + zone + " but the network's daily rewards use " + recorded
                + ": claims are refused here so no day is paid twice. Start every server with the same "
                + "-Duser.timezone, or delete the row 'zone' of daily_meta if the whole network moved.");
    }

    // ---- Players

    public @Nullable PlayerState state(UUID uuid) {
        return cache.get(uuid);
    }

    public StreakRules.Displayed displayed(PlayerState state) {
        return StreakRules.display(state, clock.today());
    }

    public void join(Player player) {
        UUID uuid = player.getUniqueId();
        sessionStart.put(uuid, System.currentTimeMillis());
        scheduler.runAsync(() -> {
            try {
                cache.put(uuid, repository.load(uuid));
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Could not load the streak of " + player.getName(), e);
                return;
            }
            scheduler.runFor(player, () -> greet(player));
        });
    }

    public void quit(Player player) {
        cache.remove(player.getUniqueId());
        sessionStart.remove(player.getUniqueId());
        claiming.remove(player.getUniqueId());
    }

    /** At join and at the reset: claims for an auto-claim player, else reminds the one who has not claimed. */
    private void greet(Player player) {
        if (!player.isOnline()) return;
        PlayerState state = cache.get(player.getUniqueId());
        if (state == null || state.claimedOn(clock.today())) return;
        if (autoClaims(state)) {
            Duration wait = playtimeLeft(player.getUniqueId());
            if (wait.isZero()) claim(player, true);
            else scheduler.runForLater(player, ticks(wait), () -> greet(player));
        } else if (settings.joinReminder()) {
            messages.send(player, "reminder");
        }
    }

    public boolean autoClaims(PlayerState state) {
        return switch (settings.autoClaim()) {
            case OFF -> false;
            case ON -> true;
            case PLAYER -> Boolean.TRUE.equals(state.autoClaim());
        };
    }

    private Duration playtimeLeft(UUID uuid) {
        int minutes = settings.minPlaytimeMinutes();
        if (minutes <= 0) return Duration.ZERO;
        long start = sessionStart.getOrDefault(uuid, System.currentTimeMillis());
        Duration played = Duration.ofMillis(System.currentTimeMillis() - start);
        Duration left = Duration.ofMinutes(minutes).minus(played);
        return left.isNegative() ? Duration.ZERO : left;
    }

    // ---- Claim

    /**
     * Claims today's reward for {@code player}, from {@code /daily claim}, the action or
     * auto-claim. Call on the player's thread; answers there.
     */
    public void claim(Player player, boolean automatic) {
        UUID uuid = player.getUniqueId();
        if (!claimsAllowed) {
            if (!automatic) messages.send(player, "claim.disabled");
            return;
        }
        long today = clock.today();
        PlayerState cached = cache.get(uuid);
        if (cached != null && cached.claimedOn(today)) {
            if (!automatic) messages.send(player, "claim.already", Map.of("time", Durations.format(clock.untilReset())));
            return;
        }
        Duration wait = playtimeLeft(uuid);
        if (!wait.isZero()) {
            if (!automatic) messages.send(player, "claim.playtime", Map.of("time", Durations.format(wait)));
            return;
        }
        if (!claiming.add(uuid)) return; // a click while the previous claim is on its way
        CalendarSettings current = settings;
        scheduler.runAsync(() -> {
            ClaimResult result;
            try {
                result = repository.claim(uuid, today, current, server, System.currentTimeMillis());
            } catch (SQLException | RuntimeException e) {
                claiming.remove(uuid);
                logger.log(Level.SEVERE, "Could not claim the daily reward of " + player.getName(), e);
                scheduler.runFor(player, () -> messages.send(player, "claim.error"));
                return;
            }
            if (result.status() != ClaimResult.Status.CONFLICT) cache.put(uuid, result.state());
            scheduler.runFor(player, () -> {
                claiming.remove(uuid);
                answer(player, result, current, automatic);
            });
            if (result.claimed()) deliver(player, result, today);
        });
    }

    private void answer(Player player, ClaimResult result, CalendarSettings current, boolean automatic) {
        switch (result.status()) {
            case ALREADY_CLAIMED -> {
                if (!automatic) messages.send(player, "claim.already", Map.of("time", Durations.format(clock.untilReset())));
            }
            case CONFLICT -> messages.send(player, "claim.conflict");
            case CLAIMED -> {
                PlayerState state = result.state();
                if (result.outcome() == StreakRules.Outcome.COVERED) {
                    messages.send(player, "claim.covered", Map.of("count", result.freezesUsed()));
                } else if (result.outcome() == StreakRules.Outcome.BROKEN) {
                    messages.send(player, "claim.broken", Map.of("lost", result.before().streak()));
                }
                messages.send(player, automatic ? "claim.success_auto" : "claim.success", Map.of(
                        "streak", state.streak(),
                        "cycle_day", result.cycleDay(),
                        "cycle_length", current.cycleLength()));
                if (result.freezeEarned()) {
                    messages.send(player, "claim.freeze_earned", Map.of("freezes", state.freezes(), "max", current.freezeMaxStock()));
                }
            }
        }
    }

    /**
     * Hands out the rewards of a claim, then records them as delivered. The rewards themselves
     * (cycle day, base, tier) come with the Artisan integration; a claim not marked delivered is
     * replayed at the player's next join.
     */
    private void deliver(Player player, ClaimResult result, long day) {
        try {
            repository.markDelivered(player.getUniqueId(), day);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not mark the claim of " + player.getName() + " delivered", e);
        }
    }

    // ---- Auto-claim choice

    /** {@code /daily autoclaim}: sets the choice, or flips it when {@code enabled} is null. */
    public void setAutoClaim(Player player, @Nullable Boolean enabled) {
        if (settings.autoClaim() != CalendarSettings.AutoClaim.PLAYER) {
            messages.send(player, "autoclaim.fixed");
            return;
        }
        UUID uuid = player.getUniqueId();
        PlayerState cached = cache.get(uuid);
        boolean next = enabled != null ? enabled : !(cached != null && Boolean.TRUE.equals(cached.autoClaim()));
        scheduler.runAsync(() -> {
            try {
                cache.put(uuid, repository.setAutoClaim(uuid, next, System.currentTimeMillis()));
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Could not save the auto-claim choice of " + player.getName(), e);
                scheduler.runFor(player, () -> messages.send(player, "command.error"));
                return;
            }
            scheduler.runFor(player, () -> {
                messages.send(player, next ? "autoclaim.enabled" : "autoclaim.disabled");
                if (next) greet(player);
            });
        });
    }

    // ---- Reset

    /** Runs {@link #onReset} when the game day ends, then again each day. */
    private void scheduleReset() {
        int generation = resetGeneration.incrementAndGet();
        Duration wait = clock.untilReset().plusSeconds(1);
        scheduler.runGlobalLater(ticks(wait), () -> {
            if (resetGeneration.get() != generation) return;
            onReset();
            scheduleReset();
        });
    }

    private void onReset() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            scheduler.runFor(player, () -> greet(player));
        }
    }

    /** Stops the reset loop (plugin disabled). */
    public void stop() {
        resetGeneration.incrementAndGet();
    }

    private static long ticks(Duration duration) {
        return Math.max(1, (duration.toMillis() + 49) / 50);
    }

    // ---- Admin

    public record Info(PlayerState state, List<LedgerEntry> ledger) {
    }

    public CompletableFuture<Info> info(UUID uuid) {
        CompletableFuture<Info> done = new CompletableFuture<>();
        scheduler.runAsync(() -> {
            try {
                done.complete(new Info(repository.load(uuid), repository.recentLedger(uuid, 10)));
            } catch (SQLException e) {
                done.completeExceptionally(e);
            }
        });
        return done;
    }

    public CompletableFuture<PlayerState> adminSet(UUID uuid, DailyRepository.AdminField field, int value, String by) {
        CompletableFuture<PlayerState> done = new CompletableFuture<>();
        scheduler.runAsync(() -> {
            try {
                PlayerState state = repository.adminSet(uuid, field, value, by, System.currentTimeMillis());
                cache.computeIfPresent(uuid, (id, old) -> state);
                done.complete(state);
            } catch (SQLException e) {
                done.completeExceptionally(e);
            }
        });
        return done;
    }

    /** {@code /dailyadmin day}: moves this server's game day; the online players are greeted again. */
    public void shiftDay(long days) {
        if (days == 0) clock.resetShift();
        else clock.shift(days);
        onReset();
    }
}
