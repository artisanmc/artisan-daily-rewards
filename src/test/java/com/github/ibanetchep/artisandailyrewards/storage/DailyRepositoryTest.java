package com.github.ibanetchep.artisandailyrewards.storage;

import com.github.ibanetchep.artisandailyrewards.core.CalendarSettings;
import com.github.ibanetchep.artisandailyrewards.core.ClaimHistory;
import com.github.ibanetchep.artisandailyrewards.core.PlayerState;
import com.github.ibanetchep.artisandailyrewards.core.StreakRules;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DailyRepositoryTest {

    private static final CalendarSettings SETTINGS = CalendarSettings.DEFAULTS;
    private static final long DAY = 20_000;

    private JdbcDataSource dataSource;
    private DailyRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        repository = new DailyRepository(dataSource, "artisan_daily_");
        repository.migrate();
    }

    @Test
    void migratingTwiceChangesNothing() throws Exception {
        UUID id = UUID.randomUUID();
        repository.checkIn(id, DAY, SETTINGS, "lobby", 1);
        repository.migrate();
        assertEquals(1, repository.load(id).streak());
        assertEquals("7", repository.meta(DailyRepository.SCHEMA_VERSION));
    }

    @Test
    void anUnknownPlayerLoadsFreshWithoutARow() throws Exception {
        UUID id = UUID.randomUUID();
        PlayerState state = repository.load(id);
        assertEquals(0, state.streak());
        assertNull(state.lastDay());
    }

    @Test
    void claimsOncePerDayAndContinuesTheNextDay() throws Exception {
        UUID id = UUID.randomUUID();
        CheckInResult first = repository.checkIn(id, DAY, SETTINGS, "lobby", 1);
        assertTrue(first.counted());
        assertEquals(StreakRules.Outcome.FIRST, first.outcome());
        assertEquals(1, first.cycleDay());

        CheckInResult again = repository.checkIn(id, DAY, SETTINGS, "survival", 2);
        assertEquals(CheckInResult.Status.ALREADY_COUNTED, again.status());
        assertEquals(1, repository.countClaims(id, DAY));

        CheckInResult next = repository.checkIn(id, DAY + 1, SETTINGS, "lobby", 3);
        assertTrue(next.counted());
        assertEquals(2, next.state().streak());
        assertEquals(next.state(), repository.load(id));
    }

    @Test
    void aCountedDayWaitsUntilItIsClaimedThenIsDeliveredOnce() throws Exception {
        UUID id = UUID.randomUUID();
        repository.checkIn(id, DAY, SETTINGS, "lobby", 1);
        repository.checkIn(id, DAY + 1, SETTINGS, "lobby", 2);
        assertEquals(2, repository.load(id).streak(), "there both days: the streak counts them unclaimed");
        assertEquals(List.of(new DailyRepository.Reward(DAY, 1, 1), new DailyRepository.Reward(DAY + 1, 2, 2)),
                repository.unclaimed(id));
        assertEquals(List.of(), repository.undelivered(id), "nothing claimed, nothing to deliver at join");
        assertFalse(repository.takeDelivery(id, DAY), "not claimed yet");

        assertEquals(List.of(new DailyRepository.Reward(DAY, 1, 1), new DailyRepository.Reward(DAY + 1, 2, 2)),
                repository.claimAll(id, 3));
        assertEquals(List.of(), repository.claimAll(id, 4), "another server claimed them");
        assertEquals(List.of(), repository.unclaimed(id));

        assertTrue(repository.takeDelivery(id, DAY));
        assertFalse(repository.takeDelivery(id, DAY), "another server took it");
        assertFalse(repository.takeDelivery(id, DAY + 5), "no such day");
        assertEquals(List.of(new DailyRepository.Reward(DAY + 1, 2, 2)), repository.undelivered(id));
    }

    @Test
    void rewardsClaimedBeforeTheMigrationStayClaimed() throws Exception {
        UUID id = UUID.randomUUID();
        try (Connection c = dataSource.getConnection(); java.sql.Statement st = c.createStatement()) {
            st.execute("INSERT INTO artisan_daily_claims (uuid, game_day, streak_after, cycle_day, server, claimed_at, delivered) "
                    + "VALUES ('" + id + "', " + DAY + ", 1, 1, 'lobby', 1, TRUE)");
        }
        assertEquals(List.of(), repository.unclaimed(id));
        assertEquals(List.of(), repository.undelivered(id));
    }

    @Test
    void theHistoryHoldsTheRecentClaimsAndTheFirstOne() throws Exception {
        UUID id = UUID.randomUUID();
        assertEquals(ClaimHistory.EMPTY, repository.history(id, DAY));
        for (long day = DAY; day < DAY + 3; day++) repository.checkIn(id, day, SETTINGS, "lobby", day);
        repository.checkIn(UUID.randomUUID(), DAY + 2, SETTINGS, "lobby", 9);

        repository.claimAll(id, 5);
        repository.checkIn(id, DAY + 3, SETTINGS, "lobby", 6);

        ClaimHistory history = repository.history(id, DAY + 1);
        assertEquals(DAY, history.firstDay());
        assertEquals(java.util.Map.of(DAY + 1, 2, DAY + 2, 3, DAY + 3, 4), history.days());
        assertEquals(java.util.Set.of(DAY + 3), history.unclaimed());
    }

    @Test
    void theHistoryKeepsEveryRewardThatWaitsHoweverOld() throws Exception {
        UUID id = UUID.randomUUID();
        repository.checkIn(id, DAY, SETTINGS, "lobby", 1);
        ClaimHistory history = repository.history(id, DAY + 30);
        assertEquals(java.util.Set.of(DAY), history.unclaimed());
        assertEquals(1, history.cycleDayOn(DAY));
    }

    @Test
    void theStateReturnedMatchesTheRowWritten() throws Exception {
        UUID id = UUID.randomUUID();
        CheckInResult result = repository.checkIn(id, DAY, SETTINGS, "lobby", 1);
        assertEquals(repository.load(id), result.state());
    }

    @Test
    void aBreakAndTheFreezesAreWrittenToTheLedger() throws Exception {
        UUID id = UUID.randomUUID();
        for (long day = DAY; day < DAY + 7; day++) repository.checkIn(id, day, SETTINGS, "lobby", day);
        assertEquals(1, repository.load(id).freezes(), "earned at 7");

        CheckInResult covered = repository.checkIn(id, DAY + 8, SETTINGS, "lobby", DAY + 8);
        assertEquals(StreakRules.Outcome.COVERED, covered.outcome());
        assertEquals(1, covered.freezesUsed());

        ClaimHistory afterFreeze = repository.history(id, DAY);
        assertTrue(afterFreeze.coveredOn(DAY + 7), "the day the freeze covered");
        assertFalse(afterFreeze.coveredOn(DAY + 6), "a counted day");

        CheckInResult broken = repository.checkIn(id, DAY + 11, SETTINGS, "lobby", DAY + 11);
        ClaimHistory afterBreak = repository.history(id, DAY);
        assertFalse(afterBreak.coveredOn(DAY + 9), "missed before a break");
        assertTrue(afterBreak.coveredOn(DAY + 7));
        assertEquals(StreakRules.Outcome.BROKEN, broken.outcome());
        assertEquals(8, broken.state().brokenStreak());

        List<LedgerEntry> ledger = repository.recentLedger(id, 10);
        assertEquals(List.of(LedgerKind.BREAK, LedgerKind.FREEZE_USED, LedgerKind.FREEZE_EARNED),
                ledger.stream().map(LedgerEntry::kind).toList());
        assertEquals(8, ledger.get(0).amount());
    }

    @Test
    void simultaneousClaimsOnTwoServersWriteOneRow() throws Exception {
        UUID id = UUID.randomUUID();
        int servers = 8;
        ExecutorService pool = Executors.newFixedThreadPool(servers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<CheckInResult>> results = new ArrayList<>();
        for (int i = 0; i < servers; i++) {
            String server = "server-" + i;
            results.add(pool.submit(() -> {
                start.await();
                return repository.checkIn(id, DAY, SETTINGS, server, 1);
            }));
        }
        start.countDown();
        int claimed = 0;
        for (Future<CheckInResult> result : results) {
            if (result.get().counted()) claimed++;
        }
        pool.shutdown();
        assertEquals(1, claimed);
        assertEquals(1, repository.countClaims(id, DAY));
        PlayerState state = repository.load(id);
        assertEquals(1, state.streak());
        assertEquals(1, state.totalDays());
    }

    @Test
    void aStaleVersionWritesNothing() throws Exception {
        UUID id = UUID.randomUUID();
        repository.checkIn(id, DAY, SETTINGS, "lobby", 1);
        PlayerState stored = repository.load(id);
        try (Connection c = dataSource.getConnection()) {
            assertEquals(0, repository.writeState(c, stored.withStreak(99), stored.version() - 1, 2));
            assertEquals(1, repository.writeState(c, stored.withStreak(99), stored.version(), 2));
        }
        assertEquals(99, repository.load(id).streak());
    }

    @Test
    void autoClaimAndAdminCorrections() throws Exception {
        UUID id = UUID.randomUUID();
        assertNull(repository.load(id).autoClaim());
        assertTrue(repository.setAutoClaim(id, true, 1).autoClaim());
        assertFalse(repository.setAutoClaim(id, false, 2).autoClaim());

        PlayerState corrected = repository.adminSet(id, DailyRepository.AdminField.STREAK, 42, "Iban", DAY, 3);
        assertEquals(42, corrected.streak());
        assertEquals(42, corrected.longest());
        assertEquals(DAY - 1, corrected.lastDay());
        LedgerEntry line = repository.recentLedger(id, 1).get(0);
        assertEquals(LedgerKind.ADMIN_SET, line.kind());
        assertEquals("streak 0 -> 42 by Iban", line.detail());

        // The set streak holds: the next day continues it.
        CheckInResult next = repository.checkIn(id, DAY, SETTINGS, "lobby", 4);
        assertEquals(StreakRules.Outcome.CONTINUED, next.outcome());
        assertEquals(43, next.state().streak());
        assertEquals(1, repository.adminSet(id, DailyRepository.AdminField.FREEZES, 1, "Iban", DAY, 5).freezes());
        assertEquals(43, repository.load(id).streak());
    }

    @Test
    void givingFreezesAddsThemOverTheStockAndLogsIt() throws Exception {
        UUID id = UUID.randomUUID();
        repository.checkIn(id, DAY, SETTINGS, "lobby", 1);
        assertEquals(2, repository.giveFreezes(id, 2, "Iban", 2).freezes());
        assertEquals(5, repository.giveFreezes(id, 3, "Iban", 3).freezes(), "over max_stock 2");
        LedgerEntry line = repository.recentLedger(id, 1).get(0);
        assertEquals(LedgerKind.FREEZE_GIVEN, line.kind());
        assertEquals(3, line.amount());
        assertEquals("freezes 2 -> 5 by Iban", line.detail());

        // Over the stock, none is earned; they are spent as usual.
        CheckInResult covered = repository.checkIn(id, DAY + 3, SETTINGS, "lobby", 4);
        assertEquals(StreakRules.Outcome.COVERED, covered.outcome());
        assertEquals(3, covered.state().freezes());
        assertEquals(2, covered.state().streak());
    }

    @Test
    void settingTheStreakRestoresABrokenOne() throws Exception {
        UUID id = UUID.randomUUID();
        for (int i = 0; i < 10; i++) repository.checkIn(id, DAY + i, SETTINGS, "a", i);
        // Five days missed, no freeze left to cover them: broken at the next count.
        CheckInResult broken = repository.checkIn(id, DAY + 15, SETTINGS, "a", 20);
        assertEquals(StreakRules.Outcome.BROKEN, broken.outcome());
        assertEquals(10, broken.state().brokenStreak());

        // Counted today already (streak 1): restored to 11, today stays its last day.
        PlayerState restored = repository.adminSet(id, DailyRepository.AdminField.STREAK, 11, "Iban", DAY + 15, 21);
        assertEquals(11, restored.streak());
        assertEquals(DAY + 15, restored.lastDay());
        assertEquals(0, restored.brokenStreak());
        assertNull(restored.brokenFromDay());
        assertEquals(11, restored.milestoneMark());
        assertEquals(StreakRules.Outcome.CONTINUED, repository.checkIn(id, DAY + 16, SETTINGS, "a", 22).outcome());
        assertEquals(12, repository.load(id).streak());
    }

    @Test
    void settingTheStreakToZeroStartsOver() {
        PlayerState s = new PlayerState(UUID.randomUUID(), 30, 30, DAY, 30, 1, 30, 0, 0, null, null, 4);
        PlayerState zero = DailyRepository.restored(s, 0, DAY + 1);
        assertEquals(0, zero.streak());
        assertEquals(0, zero.milestoneMark());
        assertEquals(30, zero.longest());
        PlayerState lower = DailyRepository.restored(s, 5, DAY + 1);
        assertEquals(30, lower.milestoneMark()); // no milestone paid twice
        assertEquals(DAY, lower.lastDay());
    }

    @Test
    void metaValuesAreReplaced() throws Exception {
        assertNull(repository.meta(DailyRepository.ZONE));
        repository.putMeta(DailyRepository.ZONE, "Europe/Paris");
        repository.putMeta(DailyRepository.ZONE, "UTC");
        assertEquals("UTC", repository.meta(DailyRepository.ZONE));
    }

    @Test
    void aDayThatReachesAMilestoneCarriesItToTheClaim() throws Exception {
        CalendarSettings d = CalendarSettings.DEFAULTS;
        CalendarSettings settings = new CalendarSettings(d.resetTime(), d.cycleLength(), d.autoClaim(), 0, true,
                d.freezeEarnEvery(), d.freezeMaxStock(), java.util.Map.of(), null,
                List.of(com.github.ibanetchep.artisandailyrewards.core.Milestone.at(2, null, null)));
        UUID id = UUID.randomUUID();
        assertFalse(repository.checkIn(id, DAY, settings, "a", 1).milestone());
        CheckInResult second = repository.checkIn(id, DAY + 1, settings, "a", 2);
        assertTrue(second.milestone());
        assertEquals(2, second.state().milestoneMark());
        assertEquals(2, repository.load(id).milestoneMark());
        assertEquals(java.util.Map.of(DAY + 1, 2), repository.history(id, DAY).milestones());
        assertEquals(List.of(new DailyRepository.Reward(DAY, 1, 1, false), new DailyRepository.Reward(DAY + 1, 2, 2, true)),
                repository.claimAll(id, 3));
    }
}
