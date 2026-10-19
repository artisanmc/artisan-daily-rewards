package com.github.ibanetchep.artisandailyrewards.storage;

import java.util.Locale;

/** What a line of the ledger table records. */
public enum LedgerKind {
    FREEZE_EARNED,
    FREEZE_USED,
    FREEZE_GIVEN,
    BREAK,
    RESTORE,
    ADMIN_SET,
    /** A kind written by a newer version of the plugin. */
    OTHER;

    public String column() {
        return name().toLowerCase(Locale.ROOT);
    }

    static LedgerKind fromColumn(String value) {
        for (LedgerKind kind : values()) {
            if (kind.column().equals(value)) return kind;
        }
        return OTHER;
    }
}
