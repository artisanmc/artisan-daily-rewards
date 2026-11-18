package com.github.ibanetchep.artisandailyrewards.core;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A bonus of {@code milestones} in {@code data/rewards.yml}, paid on top of the day's reward when
 * the streak reaches it: once per streak ({@code at: 7}), or again and again ({@code every: 30},
 * from {@code from}, which defaults to {@code every}). A break starts the streak again, and its
 * milestones with it.
 *
 * @param at      the streak of a one-time milestone, 0 for a repeating one
 * @param every   the step of a repeating milestone, 0 for a one-time one
 * @param from    the first streak a repeating milestone pays, 0 for a one-time one
 * @param icon    the item the menus show, kept as read, null for none
 * @param actions the action chain run when it is paid, kept as read, null for none
 */
public record Milestone(int at, int every, int from, @Nullable Object icon, @Nullable Object actions) {

    public static Milestone at(int streak, @Nullable Object icon, @Nullable Object actions) {
        return new Milestone(streak, 0, 0, icon, actions);
    }

    public static Milestone every(int step, int from, @Nullable Object icon, @Nullable Object actions) {
        return new Milestone(0, step, from, icon, actions);
    }

    public boolean repeats() {
        return every > 0;
    }

    /** True when a streak of {@code streak} pays this milestone. */
    public boolean matches(int streak) {
        if (streak < 1) return false;
        if (!repeats()) return streak == at;
        return streak >= from && (streak - from) % every == 0;
    }

    /** The smallest streak from {@code streak} on that pays this milestone; -1 when none does. */
    public int nextFrom(int streak) {
        int s = Math.max(streak, 1);
        if (!repeats()) return at >= s ? at : -1;
        if (s <= from) return from;
        long steps = ((long) s - from + every - 1) / every;
        long next = from + steps * every;
        return next > Integer.MAX_VALUE ? -1 : (int) next;
    }

    /** The milestones {@code streak} pays, in their order in the file. */
    public static List<Milestone> paidAt(List<Milestone> milestones, int streak) {
        List<Milestone> paid = new ArrayList<>();
        for (Milestone m : milestones) if (m.matches(streak)) paid.add(m);
        return paid;
    }

    /** One coming occurrence: the streak that pays {@code milestone}. */
    public record Occurrence(int streak, Milestone milestone) {
    }

    /**
     * The next {@code count} occurrences from streak {@code fromStreak} on, by streak then by
     * their order in the file; fewer when the one-time milestones run out.
     */
    public static List<Occurrence> upcoming(List<Milestone> milestones, int fromStreak, int count) {
        List<Occurrence> found = new ArrayList<>();
        if (count <= 0 || milestones.isEmpty()) return found;
        int[] next = new int[milestones.size()];
        for (int i = 0; i < next.length; i++) next[i] = milestones.get(i).nextFrom(fromStreak);
        while (found.size() < count) {
            int lowest = -1;
            for (int n : next) if (n > 0 && (lowest < 0 || n < lowest)) lowest = n;
            if (lowest < 0) break;
            for (int i = 0; i < next.length && found.size() < count; i++) {
                if (next[i] != lowest) continue;
                Milestone m = milestones.get(i);
                found.add(new Occurrence(lowest, m));
                next[i] = m.repeats() && lowest < Integer.MAX_VALUE ? m.nextFrom(lowest + 1) : -1;
            }
        }
        return found;
    }

    /**
     * Reads {@code milestones}: a list of sections, each with {@code at} or {@code every} (and an
     * optional {@code from}), plus {@code icon} and {@code actions}.
     */
    static List<Milestone> read(@Nullable Object value) throws CalendarException {
        if (value == null) return List.of();
        if (!(value instanceof List<?> list)) throw new CalendarException("milestones: expected a list");
        List<Milestone> milestones = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String key = "milestones[" + (i + 1) + "]";
            if (!(list.get(i) instanceof Map<?, ?> map)) {
                throw new CalendarException(key + ": expected a section with at or every, icon and actions");
            }
            Integer at = whole(map, "at", key);
            Integer every = whole(map, "every", key);
            Integer from = whole(map, "from", key);
            Object icon = map.get("icon");
            Object actions = map.get("actions");
            if (at != null && every != null) throw new CalendarException(key + ": at and every together, keep one");
            if (at != null) {
                if (from != null) throw new CalendarException(key + ": from goes with every, not at");
                milestones.add(at(at, icon, actions));
            } else if (every != null) {
                milestones.add(every(every, from != null ? from : every, icon, actions));
            } else {
                throw new CalendarException(key + ": at (once per streak) or every (again and again) is missing");
            }
        }
        return List.copyOf(milestones);
    }

    private static @Nullable Integer whole(Map<?, ?> map, String name, String key) throws CalendarException {
        Object value = map.get(name);
        if (value == null) return null;
        int parsed;
        if (value instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue())) {
            parsed = n.intValue();
        } else {
            try {
                parsed = Integer.parseInt(String.valueOf(value).trim());
            } catch (NumberFormatException e) {
                throw new CalendarException(key + "." + name + ": \"" + value + "\" is not a whole number");
            }
        }
        if (parsed < 1) throw new CalendarException(key + "." + name + ": " + parsed + " is below 1");
        return parsed;
    }
}
