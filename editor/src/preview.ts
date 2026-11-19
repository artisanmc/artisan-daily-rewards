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
 * The milestones menu's `daily:milestones` has no row without a player either: the sample
 * player's next milestones come from the draft's `milestones`.
 *
 * Mirrors `CalendarData.row`, `CalendarData.milestoneRow` and `CalendarView.around` of the plugin.
 */
import {
  type Milestone,
  type Reward,
  type RewardsValue,
  asObject,
  cycleLength,
  milestones,
  ownDays,
  paidAt,
  upcoming,
} from "./rewards";

export const CALENDAR = "daily:calendar";
export const STATUS = "daily:status";
export const MILESTONES = "daily:milestones";
const DEFAULT_MILESTONES = 28;
/** `DailyService.MAX_MILESTONES`. */
const MAX_MILESTONES = 200;
/** The sample player's streak today, not counted yet. */
const SAMPLE_TODAY = 5;
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
  /** The streak the day reaches, 0 for before / missed. */
  streak: number;
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

/** The daily folder: it holds the calendar menu or the rewards (`project-id` `addon:daily`). */
export function isDailyProject(project: unknown): boolean {
  if (rewardsOf(project)) return true;
  const p = project as { id?: unknown; ownerModule?: unknown } | undefined;
  return p?.ownerModule === "daily" || String(p?.id ?? "") === "daily" || String(p?.id ?? "").endsWith(":daily");
}

/** `StreakRules.cycleDay`. */
export function cycleDayOf(streak: number, length: number): number {
  if (streak < 1) return 1;
  return ((streak - 1) % length) + 1;
}

/** `Icons.withoutBlankTitle`: the item form's empty `title: ""` would name the item "". */
export function shownIcon(icon: unknown): unknown {
  const map = asObject(icon);
  if (!map || !("title" in map) || !blankTitle(map.title)) return icon;
  const { title: _blank, ...rest } = map;
  return rest;
}

function blankTitle(title: unknown): boolean {
  if (title == null) return true;
  if (typeof title === "string") return title.trim() === "";
  const byLang = asObject(title);
  return byLang !== undefined && Object.values(byLang).every(blankTitle);
}

type I18n = Record<string, Record<string, string>>;

/**
 * `RewardTexts.inline` of the plugin: each icon title of the rewards in every language, as a
 * `{lang: text}` map. The editor keeps the default language inline and the others at the text's
 * address (`sources.rewards.default_day.icon.title`, `sources.rewards.milestones_2.icon.title`).
 */
export function withTranslatedTitles(value: RewardsValue, i18n: I18n | undefined, defaultLang: string): RewardsValue {
  if (!i18n) return value;
  const reward = (r: unknown, address: string): unknown => {
    const obj = asObject(r);
    const icon = asObject(obj?.icon);
    if (!obj || !icon || asObject(icon.title)) return r;
    const key = `${address}.icon.title`;
    const byLang: Record<string, string> = {};
    if (typeof icon.title === "string" && icon.title.trim() !== "") byLang[defaultLang] = icon.title;
    for (const [lang, text] of Object.entries(i18n[key] ?? {})) {
      if (!(lang in byLang) && text.trim() !== "") byLang[lang] = text;
    }
    const { title: _title, ...rest } = icon;
    return { ...obj, icon: Object.keys(byLang).length > 0 ? { ...rest, title: byLang } : rest };
  };
  const out: Record<string, unknown> = { ...value };
  const base = "sources.rewards";
  if (value.default_day !== undefined) out.default_day = reward(value.default_day, `${base}.default_day`);
  const days = asObject(value.days);
  if (days) {
    out.days = Object.fromEntries(
      Object.entries(days).map(([day, r]) => [day, reward(r, `${base}.days.${day.trim().toLowerCase()}`)]),
    );
  }
  if (Array.isArray(value.milestones)) {
    out.milestones = value.milestones.map((m, i) => reward(m, `${base}.milestones${i === 0 ? "" : `_${i + 1}`}`));
  }
  return out as RewardsValue;
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

/** `CalendarView.weekShift`: days to move before today so it sits on its cycle day's column. */
export function weekShift(todayCycleDay: number, week: number): number {
  return week < 2 ? 0 : (todayCycleDay - 1) % week;
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
        ? {
            offset,
            status: told.status,
            cycleDay: told.streak > 0 ? cycleDayOf(told.streak, length) : 0,
            streak: told.streak,
          }
        : { offset, status: "before", cycleDay: 0, streak: 0 },
    );
  }
  const today = SAMPLE_TODAY;
  days.push({ offset: 0, status: "available", cycleDay: cycleDayOf(today, length), streak: today });
  for (let offset = 1; offset <= future; offset++) {
    days.push({ offset, status: "upcoming", cycleDay: cycleDayOf(today + offset, length), streak: today + offset });
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
  const icon = reward?.icon != null ? shownIcon(reward.icon) : NO_REWARD_ICON;
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
    icon,
    // The sample player's streak never broke: each streak that a milestone matches pays it.
    ...milestoneFields(milestones(value), day.streak, day.streak > 0, icon),
  };
}

/** `streak`, `is_milestone`, `milestone_count`, `milestone_icon` of a calendar row. */
function milestoneFields(list: Milestone[], streak: number, paying: boolean, dayIcon: unknown): Record<string, unknown> {
  const paid = paying ? paidAt(list, streak) : [];
  return {
    streak,
    is_milestone: paid.length > 0,
    milestone_count: paid.length,
    milestone_icon: shownIcon(paid.find((m) => m.icon != null)?.icon) ?? dayIcon,
  };
}

/** `CalendarData.milestoneRow` of the sample player's next milestones. */
export function sampleMilestones(value: RewardsValue, count: number, today: Date): Record<string, unknown>[] {
  const list = milestones(value);
  return upcoming(list, SAMPLE_TODAY, count).map(({ streak, milestone }) => {
    const offset = streak - SAMPLE_TODAY;
    const date = new Date(Date.UTC(today.getFullYear(), today.getMonth(), today.getDate()) + offset * DAY_MS);
    return {
      id: `${streak}-${list.indexOf(milestone) + 1}`,
      streak,
      offset,
      date: `${pad(date.getUTCDate())}/${pad(date.getUTCMonth() + 1)}`,
      is_today: offset === 0,
      repeats: milestone.every > 0,
      every: milestone.every,
      icon: milestone.icon != null ? shownIcon(milestone.icon) : NO_REWARD_ICON,
    };
  });
}

/** A server row with its reward taken from the draft, by its day of the cycle. */
export function withDraftReward(server: Record<string, unknown>, value: RewardsValue): Record<string, unknown> {
  const reward = rewardOf(value, Number(server.cycle_day) || 0);
  const icon = reward?.icon != null ? shownIcon(reward.icon) : NO_REWARD_ICON;
  // Whether the day pays milestones is the server's (the player's mark); which ones, the draft's.
  const fields = milestoneFields(milestones(value), Number(server.streak) || 0, server.is_milestone === true, icon);
  return { ...server, has_reward: reward !== undefined, icon, ...fields, is_milestone: server.is_milestone === true };
}

/** The sample player's `daily:status`, beside its calendar. */
export function sampleStatus(length: number, value: RewardsValue = {}): Record<string, unknown> {
  const next = upcoming(milestones(value), SAMPLE_TODAY, 1)[0];
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
    has_milestones: milestones(value).length > 0,
    next_milestone: next ? next.streak : 0,
    next_milestone_in: next ? next.streak - SAMPLE_TODAY : -1,
  };
}

/** `FrontendModule.localPreviewRows` of the daily module. */
export function dailyPreviewRows(
  project: unknown,
  ref: string,
  ctx?: { serverRows?: Record<string, unknown>[]; params?: Record<string, string> },
  today: Date = new Date(),
): Record<string, unknown>[] | null {
  if (ref !== CALENDAR && ref !== STATUS && ref !== MILESTONES) return null;
  // Not the daily folder: another module's project answers nothing.
  if (!isDailyProject(project)) return null;
  // The shipped rewards sit under originals/, which the editor does not read as a source yet:
  // the days still show, with the paper of a day without a reward.
  const p = project as { i18nKeys?: I18n; defaultLang?: string } | undefined;
  const value = withTranslatedTitles(rewardsOf(project) ?? {}, p?.i18nKeys, p?.defaultLang ?? "en");
  const length = cycleLength(value);
  const server = ctx?.serverRows;
  if (ref === STATUS) {
    // Previewed as nobody, the server's row is not loaded: the sample goes with the sample days.
    const loaded = server?.filter((r) => r.loaded === true) ?? [];
    if (loaded.length > 0) return loaded.map((r) => ({ ...r, cycle_length: length }));
    return [sampleStatus(length, value)];
  }
  if (ref === MILESTONES) {
    // The server's rows follow the saved milestones and the player's mark: kept as they are.
    if (server && server.length > 0) return server;
    const count = Math.max(0, Math.min(MAX_MILESTONES, param(ctx?.params, "count", DEFAULT_MILESTONES)));
    return sampleMilestones(value, count, today);
  }
  if (server && server.length > 0) return server.map((r) => withDraftReward(r, value));
  const past = Math.max(0, Math.min(MAX_PAST, param(ctx?.params, "past", DEFAULT_PAST)));
  const future = Math.max(0, param(ctx?.params, "future", DEFAULT_FUTURE));
  // `week`: today on the column of its day of the cycle, as `DailyService.calendar` lays it out.
  const moved = Math.min(weekShift(cycleDayOf(SAMPLE_TODAY, length), param(ctx?.params, "week", 0)), future, MAX_PAST - past);
  return sampleDays(past + moved, future - moved, length).map((day) => row(day, value, today));
}
