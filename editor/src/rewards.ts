/**
 * The rewards file (`data/rewards.yml`) as the Rewards screen reads it: `cycle_length`,
 * `days` keyed by the day of the cycle, `default_day` for the others. Pure: no React, no host.
 */

export const DEFAULT_CYCLE_LENGTH = 28;
/** A grid row: a week. */
export const WEEK = 7;

export type Reward = Record<string, unknown>;

export interface RewardsValue {
  cycle_length?: unknown;
  days?: unknown;
  default_day?: unknown;
}

/** One case of the grid. */
export interface Cell {
  day: number;
  /** The day has its own entry under `days` (a milestone). */
  own: boolean;
  /** Its reward: its own, else `default_day`; undefined when neither. */
  reward: Reward | undefined;
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
    return { day, own: own !== undefined, reward: own ?? fallback };
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
