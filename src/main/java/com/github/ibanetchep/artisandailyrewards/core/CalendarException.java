package com.github.ibanetchep.artisandailyrewards.core;

/** The calendar cannot be used as written; the message names the key. */
public final class CalendarException extends Exception {

    public CalendarException(String message) {
        super(message);
    }
}
