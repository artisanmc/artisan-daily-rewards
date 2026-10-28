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
        assertEquals("6", repository.meta(DailyRepository.SCHEMA_VERSION));
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
    void aBreakAndTheJokersAreWrittenToTheLedger() throws Exception {
        UUID id = UUID.randomUUID();
        for (long day = DAY; day < DAY + 7; day++) repository.checkIn(id, day, SETTINGS, "lobby", day);
        assertEquals(1, repository.load(id).freezes(), "earned at 7");

        CheckInResult covered = repository.checkIn(id, DAY + 8, SETTINGS, "lobby", DAY + 8);
        assertEquals(StreakRules.Outcome.COVERED, covered.outcome());
        assertEquals(1, covered.freezesUsed());

        CheckInResult broken = repository.checkIn(id, DAY + 11, SETTINGS, "lobby", DAY + 11);
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

        PlayerState corrected = repository.adminSet(id, DailyRepository.AdminField.STREAK, 42, "Iban", 3);
        assertEquals(42, corrected.streak());
        LedgerEntry line = repository.recentLedger(id, 1).get(0);
        assertEquals(LedgerKind.ADMIN_SET, line.kind());
        assertEquals("streak 0 -> 42 by Iban", line.detail());

        // A claim after a correction reads the corrected row.
        CheckInResult next = repository.checkIn(id, DAY, SETTINGS, "lobby", 4);
        assertEquals(StreakRules.Outcome.FIRST, next.outcome());
    }

    @Test
    void metaValuesAreReplaced() throws Exception {
        assertNull(repository.meta(DailyRepository.ZONE));
        repository.putMeta(DailyRepository.ZONE, "Europe/Paris");
        repository.putMeta(DailyRepository.ZONE, "UTC");
        assertEquals("UTC", repository.meta(DailyRepository.ZONE));
    }
}
