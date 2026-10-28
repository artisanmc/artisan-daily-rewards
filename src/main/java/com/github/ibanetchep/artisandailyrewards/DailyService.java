package com.github.ibanetchep.artisandailyrewards;

import com.github.ibanetchep.artisandailyrewards.artisan.DailyRewardsModule;
import com.github.ibanetchep.artisandailyrewards.core.CalendarException;
import com.github.ibanetchep.artisandailyrewards.core.CalendarSettings;
import com.github.ibanetchep.artisandailyrewards.core.CalendarView;
import com.github.ibanetchep.artisandailyrewards.core.ClaimHistory;
import com.github.ibanetchep.artisandailyrewards.core.Durations;
import com.github.ibanetchep.artisandailyrewards.core.GameClock;
import com.github.ibanetchep.artisandailyrewards.core.PlayerState;
import com.github.ibanetchep.artisandailyrewards.core.StreakRules;
import com.github.ibanetchep.artisandailyrewards.storage.CheckInResult;
import com.github.ibanetchep.artisandailyrewards.storage.DailyRepository;
import com.github.ibanetchep.artisandailyrewards.storage.LedgerEntry;
import net.artisanmc.modules.api.action.ModuleActionsApi;
import net.artisanmc.modules.api.menu.MenuApi;
import net.artisanmc.modules.api.menu.MenuRef;
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
 * day counted for the streak once the player is there (at join, after the minimum playtime, and at
 * each reset), the claim of every reward that waits (off the game threads, then back on the
 * player's thread), auto-claim, the reminder, and the calendar menu.
 *
 * <p>The database is only touched from {@link TaskScheduler#runAsync}; the cache is read from
 * any thread and answers placeholders without I/O.
 */
public final class DailyService {

    /** Where the calendar lives in the module's folder. */
    public static final String CALENDAR_PATH = "data/calendar.yaml";
    /** How far back the calendar menu can show: the claims read at join. */
    public static final int HISTORY_DAYS = 35;
    /** How far ahead the calendar menu can show. */
    public static final int MAX_FUTURE_DAYS = 366;

    private final TaskScheduler scheduler;
    private final DailyRepository repository;
    private final Messages messages;
    private final Logger logger;
    private final GameClock clock;
    private final String server;
    private final boolean sharedDatabase;
    private final ModuleActionsApi actions;
    private final MenuApi menus;

    private volatile CalendarSettings settings = CalendarSettings.DEFAULTS;
    /** False when this server's time zone differs from the network's: claiming would pay a day twice. */
    private volatile boolean claimsAllowed = true;

    private final Map<UUID, PlayerState> cache = new ConcurrentHashMap<>();
    private final Map<UUID, ClaimHistory> history = new ConcurrentHashMap<>();
    private final Map<UUID, Long> sessionStart = new ConcurrentHashMap<>();
    private final Set<UUID> claiming = ConcurrentHashMap.newKeySet();
    private final Set<UUID> checkingIn = ConcurrentHashMap.newKeySet();
    /** Raised on every reschedule: a reset task from an older schedule does nothing. */
    private final AtomicInteger resetGeneration = new AtomicInteger();

    public DailyService(TaskScheduler scheduler, DailyRepository repository, Messages messages, Logger logger,
                        GameClock clock, String server, boolean sharedDatabase, ModuleActionsApi actions,
                        MenuApi menus) {
        this.scheduler = scheduler;
        this.repository = repository;
        this.messages = messages;
        this.logger = logger;
        this.clock = clock;
        this.server = server;
        this.sharedDatabase = sharedDatabase;
        this.actions = actions;
        this.menus = menus;
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
     * Reads {@code data/calendar.yml} of the module's folder, else {@code data/calendar.yaml} (what
     * Artisan wrote before it moved to {@code .yml}). Missing: the defaults. Unreadable or wrong: the
     * previous settings stay and the error is logged. Reschedules the reset.
     */
    public void loadCalendar(@Nullable File moduleFolder) {
        File file = moduleFolder != null ? calendarFile(moduleFolder) : null;
        if (file == null) {
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

    /** {@code data/calendar.yml}, else {@code data/calendar.yaml}, else {@code null}. */
    static @Nullable File calendarFile(File moduleFolder) {
        File yml = new File(moduleFolder, CALENDAR_PATH.replace(".yaml", ".yml"));
        if (yml.isFile()) return yml;
        File yaml = new File(moduleFolder, CALENDAR_PATH);
        return yaml.isFile() ? yaml : null;
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
                + "-Duser.timezone, or delete the row 'zone' of " + repository.metaTable() + " if the whole network moved.");
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
            List<DailyRepository.Reward> pending;
            try {
                cache.put(uuid, repository.load(uuid));
                history.put(uuid, repository.history(uuid, clock.today() - HISTORY_DAYS));
                pending = repository.undelivered(uuid);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Could not load the streak of " + player.getName(), e);
                return;
            }
            // Rewards claimed but not handed out: the player left first, or a server stopped.
            CalendarSettings current = settings;
            for (DailyRepository.Reward reward : pending) deliver(player, reward, current);
            scheduler.runFor(player, () -> greet(player, true));
        });
    }

    public void quit(Player player) {
        cache.remove(player.getUniqueId());
        history.remove(player.getUniqueId());
        sessionStart.remove(player.getUniqueId());
        claiming.remove(player.getUniqueId());
        checkingIn.remove(player.getUniqueId());
    }

    /**
     * At join and at the reset: counts the day once the player has played long enough, then
     * claims for an auto-claim player, else reminds of the rewards that wait ({@code remind}).
     */
    private void greet(Player player, boolean remind) {
        if (!player.isOnline()) return;
        UUID uuid = player.getUniqueId();
        PlayerState state = cache.get(uuid);
        if (state == null) return;
        if (claimsAllowed && !state.countedOn(clock.today())) {
            Duration wait = playtimeLeft(uuid);
            if (wait.isZero()) {
                checkIn(player, remind);
                return;
            }
            scheduler.runForLater(player, ticks(wait), () -> greet(player, false));
            if (remind && !autoClaims(state) && settings.joinReminder()) remind(player, unclaimed(uuid) + 1);
            return;
        }
        offer(player, remind);
    }

    /** Claims what waits for an auto-claim player, else reminds of it. */
    private void offer(Player player, boolean remind) {
        PlayerState state = cache.get(player.getUniqueId());
        int waiting = unclaimed(player.getUniqueId());
        if (state == null || waiting == 0) return;
        if (autoClaims(state)) claim(player, true);
        else if (remind && settings.joinReminder()) remind(player, waiting);
    }

    private void remind(Player player, int waiting) {
        if (waiting > 1) messages.send(player, "reminder_many", Map.of("count", waiting));
        else messages.send(player, "reminder");
    }

    /** {@code day} counted and its reward not claimed yet, from the cache. */
    public boolean unclaimedOn(UUID uuid, long day) {
        ClaimHistory claims = history.get(uuid);
        return claims != null && claims.unclaimedOn(day);
    }

    /** The rewards of counted days that wait to be claimed, from the cache. */
    public int unclaimed(UUID uuid) {
        ClaimHistory claims = history.get(uuid);
        return claims != null ? claims.unclaimed().size() : 0;
    }

    /**
     * Counts today for the player, who is there and played long enough: off the game threads, then
     * tells them what it did to the streak and offers what waits. On a failure the day is tried
     * again at the next join or reset.
     */
    private void checkIn(Player player, boolean remind) {
        UUID uuid = player.getUniqueId();
        if (!checkingIn.add(uuid)) return;
        long today = clock.today();
        CalendarSettings current = settings;
        scheduler.runAsync(() -> {
            CheckInResult result;
            try {
                result = countDay(uuid, today, current);
            } catch (SQLException | RuntimeException e) {
                logger.log(Level.SEVERE, "Could not count the day of " + player.getName(), e);
                return;
            } finally {
                checkingIn.remove(uuid);
            }
            scheduler.runFor(player, () -> {
                announce(player, result, current);
                if (result.status() != CheckInResult.Status.CONFLICT) {
                    refreshMenu(player);
                    offer(player, remind || result.counted());
                }
            });
        });
    }

    /** Counts {@code today} in the database and the cache. Off the game threads. */
    private CheckInResult countDay(UUID uuid, long today, CalendarSettings current) throws SQLException {
        CheckInResult result = repository.checkIn(uuid, today, current, server, System.currentTimeMillis());
        if (result.status() != CheckInResult.Status.CONFLICT) cache.put(uuid, result.state());
        if (result.counted()) {
            history.compute(uuid, (id, old) -> (old != null ? old : ClaimHistory.EMPTY).with(today, result.cycleDay()));
        }
        return result;
    }

    /** What counting the day did to the streak: jokers used, a break, a joker earned. */
    private void announce(Player player, CheckInResult result, CalendarSettings current) {
        if (!result.counted()) return;
        PlayerState state = result.state();
        if (result.outcome() == StreakRules.Outcome.COVERED) {
            messages.send(player, "claim.covered", Map.of("count", result.freezesUsed()));
        } else if (result.outcome() == StreakRules.Outcome.BROKEN) {
            messages.send(player, "claim.broken", Map.of("lost", result.before().streak()));
        }
        if (result.freezeEarned()) {
            messages.send(player, "claim.freeze_earned", Map.of("freezes", state.freezes(), "max", current.freezeMaxStock()));
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
     * Claims every reward that waits for {@code player}: today's, counted first when the player
     * has played long enough, and those of the days before when they were there without claiming.
     * From {@code /daily claim}, the menu, the action or auto-claim. Call on the player's thread;
     * answers there.
     */
    public void claim(Player player, boolean automatic) {
        UUID uuid = player.getUniqueId();
        if (!claimsAllowed) {
            if (!automatic) messages.send(player, "claim.disabled");
            return;
        }
        long today = clock.today();
        PlayerState cached = cache.get(uuid);
        Duration wait = playtimeLeft(uuid);
        boolean countToday = (cached == null || !cached.countedOn(today)) && wait.isZero();
        if (!claiming.add(uuid)) return; // a click while the previous claim is on its way
        CalendarSettings current = settings;
        scheduler.runAsync(() -> {
            CheckInResult counted = null;
            List<DailyRepository.Reward> rewards;
            try {
                if (countToday) counted = countDay(uuid, today, current);
                rewards = repository.claimAll(uuid, System.currentTimeMillis());
            } catch (SQLException | RuntimeException e) {
                claiming.remove(uuid);
                logger.log(Level.SEVERE, "Could not claim the daily reward of " + player.getName(), e);
                scheduler.runFor(player, () -> messages.send(player, "claim.error"));
                return;
            }
            List<Long> days = rewards.stream().map(DailyRepository.Reward::day).toList();
            history.computeIfPresent(uuid, (id, old) -> old.claimed(days));
            for (DailyRepository.Reward reward : rewards) deliver(player, reward, current);
            CheckInResult count = counted;
            scheduler.runFor(player, () -> {
                claiming.remove(uuid);
                if (count != null) {
                    if (count.status() == CheckInResult.Status.CONFLICT) messages.send(player, "claim.conflict");
                    announce(player, count, current);
                }
                answer(player, rewards, current, automatic);
                refreshMenu(player);
            });
        });
    }

    private void answer(Player player, List<DailyRepository.Reward> rewards, CalendarSettings current, boolean automatic) {
        UUID uuid = player.getUniqueId();
        PlayerState state = cache.get(uuid);
        boolean countedToday = state != null && state.countedOn(clock.today());
        if (rewards.isEmpty()) {
            if (automatic) return;
            Duration wait = playtimeLeft(uuid);
            if (!countedToday && !wait.isZero()) messages.send(player, "claim.playtime", Map.of("time", Durations.format(wait)));
            else if (countedToday) messages.send(player, "claim.already", Map.of("time", Durations.format(clock.untilReset())));
            return;
        }
        DailyRepository.Reward last = rewards.get(rewards.size() - 1);
        if (rewards.size() == 1) {
            messages.send(player, automatic ? "claim.success_auto" : "claim.success", Map.of(
                    "streak", last.streak(),
                    "cycle_day", last.cycleDay(),
                    "cycle_length", current.cycleLength()));
        } else {
            messages.send(player, automatic ? "claim.success_many_auto" : "claim.success_many", Map.of(
                    "count", rewards.size(),
                    "streak", state != null ? state.streak() : last.streak()));
        }
        // Today could not be counted yet: its reward comes once the player has played long enough.
        Duration wait = playtimeLeft(uuid);
        if (!countedToday && !automatic && !wait.isZero()) {
            messages.send(player, "claim.playtime", Map.of("time", Durations.format(wait)));
        }
    }

    /**
     * Hands out a claimed reward: the actions of its day of the cycle ({@code days}, else
     * {@code default_day}), run by Artisan with {@code {streak}}, {@code {cycle_day}} and
     * {@code {cycle_length}} of the day it was counted. Off the game threads. The delivery is taken
     * in the database first, so two servers never pay one reward twice; a player who left keeps it
     * for their next join.
     */
    private void deliver(Player player, DailyRepository.Reward claimed, CalendarSettings current) {
        if (!player.isOnline()) return;
        int cycleDay = claimed.cycleDay();
        try {
            if (!repository.takeDelivery(player.getUniqueId(), claimed.day())) return;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not deliver the daily reward of " + player.getName()
                    + "; it is tried again at their next join", e);
            return;
        }
        CalendarSettings.DayReward reward = current.reward(cycleDay);
        if (reward == null || reward.actions() == null) return;
        actions.run(player, reward.actions(), Map.of(
                "streak", claimed.streak(),
                "cycle_day", cycleDay,
                "cycle_length", current.cycleLength()), DailyRewardsModule.FOLDER);
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
                if (next) greet(player, true);
            });
        });
    }

    // ---- Calendar menu

    /**
     * {@code past} days, today and {@code future} days for the calendar menu, from the cache:
     * null until the player's streak is loaded. Capped to {@link #HISTORY_DAYS} and
     * {@link #MAX_FUTURE_DAYS}.
     */
    public @Nullable List<CalendarView.Day> calendar(UUID uuid, int past, int future) {
        PlayerState state = cache.get(uuid);
        if (state == null) return null;
        ClaimHistory claims = history.getOrDefault(uuid, ClaimHistory.EMPTY);
        return CalendarView.around(state, claims, clock.today(), settings.cycleLength(),
                Math.clamp(past, 0, HISTORY_DAYS), Math.clamp(future, 0, MAX_FUTURE_DAYS));
    }

    /** Opens again a menu of the module's folder the player has open: what it shows changed. */
    private void refreshMenu(Player player) {
        MenuRef open = menus.openMenu(player);
        if (open != null && DailyRewardsModule.FOLDER.equals(open.getProjectId())) menus.open(player, open, Map.of());
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
            scheduler.runFor(player, () -> greet(player, true));
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
