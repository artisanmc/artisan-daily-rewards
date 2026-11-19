/**
 * The rewards file (`data/rewards.yml`) as the Rewards screen reads it: `cycle_length`,
 * `days` keyed by the day of the cycle, `default_day` for the others, `milestones` of the streak. Pure: no React, no host.
 */

export const DEFAULT_CYCLE_LENGTH = 28;
/** A grid row: a week. */
export const WEEK = 7;

export type Reward = Record<string, unknown>;

export interface RewardsValue {
  cycle_length?: unknown;
  days?: unknown;
  default_day?: unknown;
  with_default_day?: unknown;
  milestones?: unknown;
}

/** One case of the grid. */
export interface Cell {
  day: number;
  /** The day has its own entry under `days` (a milestone). */
  own: boolean;
  /** Its reward: its own, else `default_day`; undefined when neither. */
  reward: Reward | undefined;
  /** An own day that also pays `default_day` (`stacksDefault`), with a `default_day` to pay. */
  stacks: boolean;
}

/** The key, at the top of the file and under a day, that pays `default_day` with a day's own reward. */
export const WITH_DEFAULT_DAY = "with_default_day";

/** `with_default_day` at the top of the file: false when missing or not a boolean. */
export function withDefaultDay(value: RewardsValue): boolean {
  return value.with_default_day === true;
}

/** `CalendarSettings.stacksDefault`: the day's own setting, else the file's. */
export function stacksDefault(value: RewardsValue, own: Reward): boolean {
  const mine = own[WITH_DEFAULT_DAY];
  return typeof mine === "boolean" ? mine : withDefaultDay(value);
}

export function asObject(value: unknown): Record<string, unknown> | undefined {
  return value && typeof value === "object" && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : undefined;
}

/** `cycle_length`, a whole number from 1; the plugin's default when missing or wrong. */
export function cycleLength(value: RewardsValue): number {
  const n = Number(value.cycle_length);
  return Number.isInteger(n) && n >= 1 ? n : DEFAULT_CYCLE_LENGTH;
}

/** `days` by day number; a key that is not a whole number from 1 is left out. */
export function ownDays(value: RewardsValue): Map<number, Reward> {
  const days = new Map<number, Reward>();
  for (const [key, reward] of Object.entries(asObject(value.days) ?? {})) {
    const day = Number(key);
    if (Number.isInteger(day) && day >= 1) days.set(day, asObject(reward) ?? {});
  }
  return days;
}

/** Days 1 to the cycle length, each with the reward a claim of it pays. */
export function cells(value: RewardsValue): Cell[] {
  const length = cycleLength(value);
  const days = ownDays(value);
  const fallback = asObject(value.default_day);
  return Array.from({ length }, (_, i) => {
    const day = i + 1;
    const own = days.get(day);
    const stacks = own !== undefined && fallback !== undefined && stacksDefault(value, own);
    return { day, own: own !== undefined, reward: own ?? fallback, stacks };
  });
}

/** Days listed under `days` past the cycle: the plugin refuses the file until they go. */
export function daysOutside(value: RewardsValue): number[] {
  const length = cycleLength(value);
  return [...ownDays(value).keys()].filter((d) => d > length).sort((a, b) => a - b);
}

/**
 * `days` with `day` set to `reward`, keys in day order. The original keys are kept as
 * written (`"7"` or `7`), so an untouched day writes back the same.
 */
export function withDay(value: RewardsValue, day: number, reward: Reward | undefined): Record<string, unknown> {
  const entries = Object.entries(asObject(value.days) ?? {}).filter(([key]) => Number(key) !== day);
  if (reward !== undefined) entries.push([String(day), reward]);
  entries.sort(([a], [b]) => Number(a) - Number(b));
  return Object.fromEntries(entries);
}

/** A deep copy, so a day made from the default one never shares its objects. */
export function copyOf<T>(value: T): T {
  return value === undefined ? value : (JSON.parse(JSON.stringify(value)) as T);
}

/** The item an icon names: the editor holds it under `extends`, the file under `material`. */
export function iconMaterial(icon: unknown): string {
  if (typeof icon === "string") return icon;
  const item = asObject(icon);
  const source = item?.extends ?? item?.item ?? item?.material;
  return typeof source === "string" ? source : "";
}

export function iconCount(icon: unknown): number {
  const count = asObject(icon)?.count;
  return typeof count === "number" && count > 1 ? count : 1;
}

/** Steps of a reward's `actions`. */
export function actionCount(reward: Reward | undefined): number {
  return Array.isArray(reward?.actions) ? reward.actions.length : 0;
}

/**
 * The icon's title in `lang`: a literal, a text of the Project (`{key}`, read with `text`), or a
 * map by language as the file writes it. "" for none.
 */
export function iconTitle(icon: unknown, lang: string, text: (key: string) => string): string {
  const title = asObject(icon)?.title;
  if (typeof title === "string") return title;
  const node = asObject(title);
  if (!node) return "";
  if (typeof node.key === "string") return text(node.key);
  const own = node[lang];
  if (typeof own === "string") return own;
  const first = Object.values(node).find((v) => typeof v === "string");
  return typeof first === "string" ? first : "";
}

/**
 * A copy of `reward` for another day: the icon's title and lore, when they are texts of the
 * Project (`{key}`), become their text (`text(key)`), so the copy gets texts of its own and
 * editing one day never changes the other.
 */
export function detachTexts(reward: Reward | undefined, text: (key: string) => string): Reward | undefined {
  const copy = copyOf(reward);
  const icon = asObject(copy?.icon);
  if (!icon) return copy;
  for (const field of ["title", "lore"]) {
    const node = asObject(icon[field]);
    if (node && typeof node.key === "string") icon[field] = text(node.key);
  }
  return copy;
}

// ---- Milestones

/** A milestone of `milestones`, as the plugin reads it (`Milestone.read`). */
export interface Milestone {
  /** The streak of a one-time milestone, 0 for a repeating one. */
  at: number;
  /** The step of a repeating milestone, 0 for a one-time one. */
  every: number;
  /** The first streak a repeating milestone pays, `every` when the file leaves it out. */
  from: number;
  icon: unknown;
  actions: unknown;
}

function whole(value: unknown): number {
  const n = Number(value);
  return Number.isInteger(n) && n >= 1 ? n : 0;
}

/** The entries of `milestones` as written: a list of sections (anything else: none). */
export function milestoneEntries(value: RewardsValue): Reward[] {
  const list = value.milestones;
  return Array.isArray(list) ? list.map((m) => asObject(m) ?? {}) : [];
}

/** One entry read: `at` wins when both are there (the plugin refuses that file), none when neither. */
export function readMilestone(entry: Reward): Milestone | undefined {
  const at = whole(entry.at);
  const every = whole(entry.every);
  if (at > 0) return { at, every: 0, from: 0, icon: entry.icon, actions: entry.actions };
  if (every > 0) return { at: 0, every, from: whole(entry.from) || every, icon: entry.icon, actions: entry.actions };
  return undefined;
}

/** The milestones the plugin pays, in the file's order; an unreadable entry is left out. */
export function milestones(value: RewardsValue): Milestone[] {
  return milestoneEntries(value)
    .map(readMilestone)
    .filter((m): m is Milestone => m !== undefined);
}

/** `Milestone.matches`. */
export function matches(m: Milestone, streak: number): boolean {
  if (streak < 1) return false;
  if (m.every === 0) return streak === m.at;
  return streak >= m.from && (streak - m.from) % m.every === 0;
}

/** `Milestone.nextFrom`: the smallest streak from `streak` on that pays `m`, -1 for none. */
export function nextFrom(m: Milestone, streak: number): number {
  const s = Math.max(streak, 1);
  if (m.every === 0) return m.at >= s ? m.at : -1;
  if (s <= m.from) return m.from;
  return m.from + Math.ceil((s - m.from) / m.every) * m.every;
}

/** The milestones a streak pays, in the file's order. */
export function paidAt(list: Milestone[], streak: number): Milestone[] {
  return list.filter((m) => matches(m, streak));
}

/** `Milestone.upcoming`: the next `count` (streak, milestone), by streak then file order. */
export function upcoming(list: Milestone[], fromStreak: number, count: number): { streak: number; milestone: Milestone }[] {
  const found: { streak: number; milestone: Milestone }[] = [];
  const next = list.map((m) => nextFrom(m, fromStreak));
  while (found.length < count) {
    const candidates = next.filter((n) => n > 0);
    if (candidates.length === 0) break;
    const lowest = Math.min(...candidates);
    for (let i = 0; i < next.length && found.length < count; i++) {
      if (next[i] !== lowest) continue;
      const m = list[i];
      found.push({ streak: lowest, milestone: m });
      next[i] = m.every > 0 ? nextFrom(m, lowest + 1) : -1;
    }
  }
  return found;
}

/**
 * A one-time milestone is paid again after a break: when none repeats, a player past the last
 * one earns more by breaking their streak than by going on. True when there are milestones and
 * none of them repeats.
 */
export function noneRepeats(list: Milestone[]): boolean {
  return list.length > 0 && !list.some((m) => m.every > 0);
}

/** A short label: "At 30" / "Every 50 from 100". */
export function milestoneRule(m: Milestone, t: (key: "milestoneAt" | "milestoneEvery" | "milestoneEveryFrom", vars: Record<string, number>) => string): string {
  if (m.every === 0) return t("milestoneAt", { at: m.at });
  return m.from === m.every ? t("milestoneEvery", { every: m.every }) : t("milestoneEveryFrom", { every: m.every, from: m.from });
}
