package com.github.ibanetchep.artisandailyrewards.storage;

import com.github.ibanetchep.artisandailyrewards.core.CalendarSettings;
import com.github.ibanetchep.artisandailyrewards.core.PlayerState;
import com.github.ibanetchep.artisandailyrewards.core.StreakRules;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The streaks, in the database Artisan lends to the suite (H2, MySQL or MariaDB), in tables
 * prefixed {@code daily_}. Plain JDBC with statements every dialect runs the same: no upsert.
 *
 * <p>The primary key {@code (uuid, game_day)} of {@code daily_claims} is what guarantees one claim per
 * player and game day, across every server of a network; the {@code version} column of
 * {@code daily_players} keeps two servers from overwriting each other's counters.
 *
 * <p>Every method blocks: call them off the game threads.
 */
public final class DailyRepository {

    static final String PLAYERS = "daily_players";
    static final String CLAIMS = "daily_claims";
    static final String LEDGER = "daily_ledger";
    static final String META = "daily_meta";
    static final String SCHEMA_VERSION = "schema_version";
    public static final String ZONE = "zone";

    /** Forward only: append, never edit a released step. */
    private static final List<String> MIGRATIONS = List.of(
            "CREATE TABLE " + PLAYERS + " ("
                    + "uuid CHAR(36) NOT NULL PRIMARY KEY, "
                    + "streak INT NOT NULL DEFAULT 0, "
                    + "longest INT NOT NULL DEFAULT 0, "
                    + "last_day INT NULL, "
                    + "total_claims INT NOT NULL DEFAULT 0, "
                    + "freezes INT NOT NULL DEFAULT 0, "
                    + "milestone_mark INT NOT NULL DEFAULT 0, "
                    + "broken_streak INT NOT NULL DEFAULT 0, "
                    + "broken_mark INT NOT NULL DEFAULT 0, "
                    + "broken_from_day INT NULL, "
                    + "auto_claim BOOLEAN NULL, "
                    + "version INT NOT NULL DEFAULT 0, "
                    + "updated_at BIGINT NOT NULL DEFAULT 0)",
            "CREATE TABLE " + CLAIMS + " ("
                    + "uuid CHAR(36) NOT NULL, "
                    + "game_day INT NOT NULL, "
                    + "streak_after INT NOT NULL, "
                    + "cycle_day INT NOT NULL, "
                    + "server VARCHAR(64) NOT NULL, "
                    + "claimed_at BIGINT NOT NULL, "
                    + "delivered BOOLEAN NOT NULL DEFAULT FALSE, "
                    + "PRIMARY KEY (uuid, game_day))",
            "CREATE TABLE " + LEDGER + " ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                    + "uuid CHAR(36) NOT NULL, "
                    + "created_at BIGINT NOT NULL, "
                    + "kind VARCHAR(32) NOT NULL, "
                    + "amount INT NOT NULL, "
                    + "detail VARCHAR(255) NULL)",
            "CREATE INDEX daily_ledger_player ON " + LEDGER + " (uuid, created_at)",
            "CREATE INDEX daily_players_streak ON " + PLAYERS + " (streak)");

    private static final String STATE_COLUMNS = "streak, longest, last_day, total_claims, freezes, milestone_mark, "
            + "broken_streak, broken_mark, broken_from_day, auto_claim, version";

    private final DataSource dataSource;

    public DailyRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Creates or upgrades the tables; the step reached is {@code schema_version} in {@code daily_meta}. */
    public void migrate() throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            try (Statement s = c.createStatement()) {
                s.execute("CREATE TABLE IF NOT EXISTS " + META + " ("
                        + "meta_key VARCHAR(64) NOT NULL PRIMARY KEY, meta_value VARCHAR(255) NOT NULL)");
            }
            String stored = meta(c, SCHEMA_VERSION);
            int current = stored != null ? Integer.parseInt(stored) : 0;
            for (int i = current; i < MIGRATIONS.size(); i++) {
                try (Statement s = c.createStatement()) {
                    s.execute(MIGRATIONS.get(i));
                }
                putMeta(c, SCHEMA_VERSION, String.valueOf(i + 1));
            }
        }
    }

    /** The player's row, or a fresh state when they never claimed (no row is written). */
    public PlayerState load(UUID uuid) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            PlayerState state = select(c, uuid);
            return state != null ? state : PlayerState.fresh(uuid);
        }
    }

    /**
     * Claims for {@code uuid} on {@code today} in one transaction: the rule in memory, a row in
     * {@code daily_claims} (a duplicate key means already claimed, on this server or another), the
     * counters written only if no other server wrote them since they were read, then the ledger.
     * A concurrent write is retried once.
     */
    public ClaimResult claim(UUID uuid, long today, CalendarSettings settings, String server, long now)
            throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            ensureRow(c, uuid, now);
            boolean autoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                for (int attempt = 0; attempt < 2; attempt++) {
                    PlayerState before = select(c, uuid);
                    if (before == null) throw new SQLException("daily_players row of " + uuid + " vanished");
                    StreakRules.Claim claim = StreakRules.claim(before, today, settings);
                    if (!claim.claimed()) {
                        c.rollback();
                        return ClaimResult.alreadyClaimed(before);
                    }
                    PlayerState after = claim.state();
                    int cycleDay = StreakRules.cycleDay(after.streak(), settings.cycleLength());
                    try {
                        insertClaim(c, uuid, today, after.streak(), cycleDay, server, now);
                    } catch (SQLException e) {
                        c.rollback();
                        if (!isDuplicateKey(e)) throw e;
                        PlayerState current = select(c, uuid);
                        return ClaimResult.alreadyClaimed(current != null ? current : before);
                    }
                    if (writeState(c, after, before.version(), now) == 0) {
                        c.rollback(); // another server wrote this player since the read
                        continue;
                    }
                    int freezesUsed = claim.freezesUsed();
                    if (freezesUsed > 0) ledger(c, uuid, now, LedgerKind.FREEZE_USED, freezesUsed, "day " + today);
                    if (claim.outcome() == StreakRules.Outcome.BROKEN) {
                        ledger(c, uuid, now, LedgerKind.BREAK, before.streak(), "from day " + after.brokenFromDay());
                    }
                    if (claim.freezeEarned()) ledger(c, uuid, now, LedgerKind.FREEZE_EARNED, 1, "streak " + after.streak());
                    c.commit();
                    return ClaimResult.claimed(before, after.withVersion(before.version() + 1), claim.outcome(),
                            freezesUsed, claim.freezeEarned(), cycleDay);
                }
                return ClaimResult.conflict(PlayerState.fresh(uuid));
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(autoCommit);
            }
        }
    }

    /** The rewards of that claim were handed out. */
    public void markDelivered(UUID uuid, long day) throws SQLException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "UPDATE " + CLAIMS + " SET delivered = TRUE WHERE uuid = ? AND game_day = ?")) {
            p.setString(1, uuid.toString());
            p.setLong(2, day);
            p.executeUpdate();
        }
    }

    /** Stores the player's auto-claim choice. */
    public PlayerState setAutoClaim(UUID uuid, boolean enabled, long now) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            ensureRow(c, uuid, now);
            try (PreparedStatement p = c.prepareStatement("UPDATE " + PLAYERS
                    + " SET auto_claim = ?, version = version + 1, updated_at = ? WHERE uuid = ?")) {
                p.setBoolean(1, enabled);
                p.setLong(2, now);
                p.setString(3, uuid.toString());
                p.executeUpdate();
            }
            return select(c, uuid);
        }
    }

    public enum AdminField {
        STREAK("streak"), LONGEST("longest"), FREEZES("freezes");

        final String column;

        AdminField(String column) {
            this.column = column;
        }
    }

    /** An admin's correction, written to the ledger with the old and new value. */
    public PlayerState adminSet(UUID uuid, AdminField field, int value, String by, long now) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            ensureRow(c, uuid, now);
            PlayerState before = select(c, uuid);
            int old = switch (field) {
                case STREAK -> before.streak();
                case LONGEST -> before.longest();
                case FREEZES -> before.freezes();
            };
            try (PreparedStatement p = c.prepareStatement("UPDATE " + PLAYERS + " SET " + field.column
                    + " = ?, version = version + 1, updated_at = ? WHERE uuid = ?")) {
                p.setInt(1, value);
                p.setLong(2, now);
                p.setString(3, uuid.toString());
                p.executeUpdate();
            }
            ledger(c, uuid, now, LedgerKind.ADMIN_SET, value, field.column + " " + old + " -> " + value + " by " + by);
            return select(c, uuid);
        }
    }

    /** The {@code limit} latest ledger lines of a player, newest first. */
    public List<LedgerEntry> recentLedger(UUID uuid, int limit) throws SQLException {
        List<LedgerEntry> entries = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT created_at, kind, amount, detail FROM " + LEDGER
                     + " WHERE uuid = ? ORDER BY created_at DESC, id DESC")) {
            p.setString(1, uuid.toString());
            p.setMaxRows(limit);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    entries.add(new LedgerEntry(r.getLong(1), LedgerKind.fromColumn(r.getString(2)), r.getInt(3), r.getString(4)));
                }
            }
        }
        return entries;
    }

    /** Claims recorded for a player and day; 0 or 1, the primary key sees to it. */
    public int countClaims(UUID uuid, long day) throws SQLException {
        try (Connection c = dataSource.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM " + CLAIMS + " WHERE uuid = ? AND game_day = ?")) {
            p.setString(1, uuid.toString());
            p.setLong(2, day);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    public @Nullable String meta(String key) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            return meta(c, key);
        }
    }

    public void putMeta(String key, String value) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            putMeta(c, key, value);
        }
    }

    private static @Nullable String meta(Connection c, String key) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT meta_value FROM " + META + " WHERE meta_key = ?")) {
            p.setString(1, key);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getString(1) : null;
            }
        }
    }

    private static void putMeta(Connection c, String key, String value) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("UPDATE " + META + " SET meta_value = ? WHERE meta_key = ?")) {
            p.setString(1, value);
            p.setString(2, key);
            if (p.executeUpdate() > 0) return;
        }
        try (PreparedStatement p = c.prepareStatement("INSERT INTO " + META + " (meta_key, meta_value) VALUES (?, ?)")) {
            p.setString(1, key);
            p.setString(2, value);
            p.executeUpdate();
        } catch (SQLException e) {
            if (!isDuplicateKey(e)) throw e;
            putMeta(c, key, value); // written meanwhile by another server
        }
    }

    /** Creates the player's row when missing; another server creating it first is fine. */
    private static void ensureRow(Connection c, UUID uuid, long now) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM " + PLAYERS + " WHERE uuid = ?")) {
            p.setString(1, uuid.toString());
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) return;
            }
        }
        try (PreparedStatement p = c.prepareStatement("INSERT INTO " + PLAYERS + " (uuid, updated_at) VALUES (?, ?)")) {
            p.setString(1, uuid.toString());
            p.setLong(2, now);
            p.executeUpdate();
        } catch (SQLException e) {
            if (!isDuplicateKey(e)) throw e;
        }
    }

    private static @Nullable PlayerState select(Connection c, UUID uuid) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT " + STATE_COLUMNS + " FROM " + PLAYERS + " WHERE uuid = ?")) {
            p.setString(1, uuid.toString());
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return null;
                return new PlayerState(uuid, r.getInt(1), r.getInt(2), nullableLong(r, 3), r.getInt(4), r.getInt(5),
                        r.getInt(6), r.getInt(7), r.getInt(8), nullableLong(r, 9), nullableBoolean(r, 10), r.getInt(11));
            }
        }
    }

    private static void insertClaim(Connection c, UUID uuid, long day, int streak, int cycleDay, String server, long now)
            throws SQLException {
        try (PreparedStatement p = c.prepareStatement("INSERT INTO " + CLAIMS
                + " (uuid, game_day, streak_after, cycle_day, server, claimed_at, delivered) VALUES (?, ?, ?, ?, ?, ?, FALSE)")) {
            p.setString(1, uuid.toString());
            p.setLong(2, day);
            p.setInt(3, streak);
            p.setInt(4, cycleDay);
            p.setString(5, server);
            p.setLong(6, now);
            p.executeUpdate();
        }
    }

    /** Writes the counters if the row still has {@code expectedVersion}; returns the rows changed. */
    static int writeState(Connection c, PlayerState s, int expectedVersion, long now) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("UPDATE " + PLAYERS + " SET streak = ?, longest = ?, last_day = ?, "
                + "total_claims = ?, freezes = ?, milestone_mark = ?, broken_streak = ?, broken_mark = ?, "
                + "broken_from_day = ?, version = ?, updated_at = ? WHERE uuid = ? AND version = ?")) {
            p.setInt(1, s.streak());
            p.setInt(2, s.longest());
            setNullableLong(p, 3, s.lastDay());
            p.setInt(4, s.totalClaims());
            p.setInt(5, s.freezes());
            p.setInt(6, s.milestoneMark());
            p.setInt(7, s.brokenStreak());
            p.setInt(8, s.brokenMark());
            setNullableLong(p, 9, s.brokenFromDay());
            p.setInt(10, expectedVersion + 1);
            p.setLong(11, now);
            p.setString(12, s.uuid().toString());
            p.setInt(13, expectedVersion);
            return p.executeUpdate();
        }
    }

    private static void ledger(Connection c, UUID uuid, long at, LedgerKind kind, int amount, @Nullable String detail)
            throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO " + LEDGER + " (uuid, created_at, kind, amount, detail) VALUES (?, ?, ?, ?, ?)")) {
            p.setString(1, uuid.toString());
            p.setLong(2, at);
            p.setString(3, kind.column());
            p.setInt(4, amount);
            p.setString(5, detail);
            p.executeUpdate();
        }
    }

    private static void setNullableLong(PreparedStatement p, int index, @Nullable Long value) throws SQLException {
        if (value == null) p.setNull(index, Types.INTEGER);
        else p.setLong(index, value);
    }

    private static @Nullable Long nullableLong(ResultSet r, int column) throws SQLException {
        long value = r.getLong(column);
        return r.wasNull() ? null : value;
    }

    private static @Nullable Boolean nullableBoolean(ResultSet r, int column) throws SQLException {
        boolean value = r.getBoolean(column);
        return r.wasNull() ? null : value;
    }

    /** SQLState class 23 (integrity constraint violation): H2, MySQL and MariaDB all report a duplicate key so. */
    static boolean isDuplicateKey(SQLException e) {
        return e.getSQLState() != null && e.getSQLState().startsWith("23");
    }
}
