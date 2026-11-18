/**
 * The rows the editor previews the calendar menu with (`FrontendModule.localPreviewRows`), from
 * the draft of `data/rewards`. Pure: no React, no host.
 *
 * The plugin's `daily:calendar` has no row without a player: previewed as nobody (the canvas'
 * default), the server answers an empty list, so the 28 days of the menu stayed empty and
 * `{d.icon}` unresolved. Here:
 * - the server's rows, when it has some (previewed as a player), keep their days as they went and
 *   take each day's reward from the draft;
 * - without them, a sample player shows every state of a day: before the first day counted,
 *   claimed, missed (a joker kept the streak), waiting to be claimed, today and the days to come.
 *
 * Mirrors `CalendarData.row` and `CalendarView.around` of the plugin.
 */
import { type Reward, type RewardsValue, asObject, cycleLength, ownDays } from "./rewards";

export const CALENDAR = "daily:calendar";
export const STATUS = "daily:status";
/** The rewards file, a config source of the daily folder. */
export const REWARDS_SOURCE = "rewards";
const DEFAULT_PAST = 7;
const DEFAULT_FUTURE = 20;
/** `DailyService.HISTORY_DAYS`: the most past days the plugin returns. */
const MAX_PAST = 35;
/** `CalendarData.NO_REWARD_ICON`. */
export const NO_REWARD_ICON = { material: "PAPER" };
const DAY_MS = 86_400_000;

export type Status = "before" | "claimed" | "missed" | "available" | "upcoming";

/** One day of the sample player, `offset` days from today. */
export interface SampleDay {
  offset: number;
  status: Status;
  cycleDay: number;
}

interface ProjectSources {
  dataSources?: { id: unknown; shape?: string; value?: unknown }[];
}

/** The rewards draft of `project`, undefined when it holds none. */
export function rewardsOf(project: unknown): RewardsValue | undefined {
  const source = (project as ProjectSources | undefined)?.dataSources?.find(
    (d) => String(d.id) === REWARDS_SOURCE,
  );
  if (!source || source.shape !== "config") return undefined;
  return asObject(source.value) as RewardsValue | undefined;
}

/** `StreakRules.cycleDay`. */
export function cycleDayOf(streak: number, length: number): number {
  if (streak < 1) return 1;
  return ((streak - 1) % length) + 1;
}

/** The reward of a day of the cycle: its own, else `default_day`; none for day 0. */
export function rewardOf(value: RewardsValue, cycleDay: number): Reward | undefined {
  if (cycleDay < 1) return undefined;
  return ownDays(value).get(cycleDay) ?? asObject(value.default_day);
}

/** A param of the menu's input as the plugin reads it, else `fallback`. */
export function param(params: Record<string, string> | undefined, key: string, fallback: number): number {
  const raw = params?.[key];
  if (raw === undefined) return fallback;
  const n = Number.parseInt(String(raw).trim(), 10);
  return Number.isNaN(n) ? fallback : n;
}

/**
 * The sample player's days: `past` days before today, today, `future` days after. Its streak
 * started 5 days ago; it missed the day before yesterday, a joker kept the streak; it was there
 * yesterday without claiming, and today waits too.
 */
export function sampleDays(past: number, future: number, length: number): SampleDay[] {
  // Oldest first, ending yesterday: the streak these days reach (0 for before / missed).
  const story: { status: Status; streak: number }[] = [
    { status: "claimed", streak: 1 },
    { status: "claimed", streak: 2 },
    { status: "claimed", streak: 3 },
    { status: "missed", streak: 0 },
    { status: "available", streak: 4 },
  ];
  const days: SampleDay[] = [];
  for (let offset = -past; offset < 0; offset++) {
    const told = story[story.length + offset];
    days.push(
      told
        ? { offset, status: told.status, cycleDay: told.streak > 0 ? cycleDayOf(told.streak, length) : 0 }
        : { offset, status: "before", cycleDay: 0 },
    );
  }
  const today = 5;
  days.push({ offset: 0, status: "available", cycleDay: cycleDayOf(today, length) });
  for (let offset = 1; offset <= future; offset++) {
    days.push({ offset, status: "upcoming", cycleDay: cycleDayOf(today + offset, length) });
  }
  return days;
}

function pad(n: number): string {
  return String(n).padStart(2, "0");
}

/** `CalendarData.row` for a sample day, `today` being the preview's date. */
export function row(day: SampleDay, value: RewardsValue, today: Date): Record<string, unknown> {
  const date = new Date(Date.UTC(today.getFullYear(), today.getMonth(), today.getDate()) + day.offset * DAY_MS);
  const reward = rewardOf(value, day.cycleDay);
  return {
    id: `${date.getUTCFullYear()}-${pad(date.getUTCMonth() + 1)}-${pad(date.getUTCDate())}`,
    date: `${pad(date.getUTCDate())}/${pad(date.getUTCMonth() + 1)}`,
    offset: day.offset,
    status: day.status,
    is_today: day.offset === 0,
    is_claimed: day.status === "claimed",
    is_missed: day.status === "missed",
    is_before: day.status === "before",
    is_available: day.status === "available",
    is_upcoming: day.status === "upcoming",
    cycle_day: day.cycleDay,
    has_reward: reward !== undefined,
    icon: reward?.icon ?? NO_REWARD_ICON,
  };
}

/** A server row with its reward taken from the draft, by its day of the cycle. */
export function withDraftReward(server: Record<string, unknown>, value: RewardsValue): Record<string, unknown> {
  const reward = rewardOf(value, Number(server.cycle_day) || 0);
  return { ...server, has_reward: reward !== undefined, icon: reward?.icon ?? NO_REWARD_ICON };
}

/** The sample player's `daily:status`, beside its calendar. */
export function sampleStatus(length: number): Record<string, unknown> {
  return {
    id: "status",
    loaded: true,
    streak: 4,
    longest: 12,
    total: 31,
    freezes: 0,
    freeze_max: 3,
    claimed_today: false,
    unclaimed: 2,
    can_claim: true,
    at_risk: false,
    broken: false,
    lost: 0,
    cycle_day: cycleDayOf(5, length),
    cycle_length: length,
    until_reset: "3h 12m",
  };
}

/** `FrontendModule.localPreviewRows` of the daily module. */
export function dailyPreviewRows(
  project: unknown,
  ref: string,
  ctx?: { serverRows?: Record<string, unknown>[]; params?: Record<string, string> },
  today: Date = new Date(),
): Record<string, unknown>[] | null {
  if (ref !== CALENDAR && ref !== STATUS) return null;
  const value = rewardsOf(project);
  if (!value) return null;
  const length = cycleLength(value);
  const server = ctx?.serverRows;
  if (ref === STATUS) {
    // Previewed as nobody, the server's row is not loaded: the sample goes with the sample days.
    const loaded = server?.filter((r) => r.loaded === true) ?? [];
    if (loaded.length > 0) return loaded.map((r) => ({ ...r, cycle_length: length }));
    return [sampleStatus(length)];
  }
  if (server && server.length > 0) return server.map((r) => withDraftReward(r, value));
  const past = Math.max(0, Math.min(MAX_PAST, param(ctx?.params, "past", DEFAULT_PAST)));
  const future = Math.max(0, param(ctx?.params, "future", DEFAULT_FUTURE));
  return sampleDays(past, future, length).map((day) => row(day, value, today));
}
