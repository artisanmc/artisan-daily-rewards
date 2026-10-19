package com.github.ibanetchep.artisandailyrewards.storage;

import org.jetbrains.annotations.Nullable;

/** One line of {@code daily_ledger}: {@code at} in epoch milliseconds. */
public record LedgerEntry(long at, LedgerKind kind, int amount, @Nullable String detail) {
}
