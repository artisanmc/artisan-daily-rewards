import { describe, expect, it } from "vitest";
import { NO_REWARD_ICON, dailyPreviewRows, sampleDays, weekShift } from "./preview";

const bread = { icon: { extends: "BREAD", count: 8 } };
const diamond = { icon: { material: "DIAMOND", count: 3 } };
const project = {
  dataSources: [{ id: "rewards", shape: "config", value: { cycle_length: 7, days: { "7": diamond }, default_day: bread } }],
};
const today = new Date(2026, 10, 18);

describe("the calendar preview", () => {
  it("answers only the daily sources, and only with a rewards draft", () => {
    expect(dailyPreviewRows(project, "boutique:items")).toBeNull();
    expect(dailyPreviewRows({ id: "guilds", dataSources: [] }, "daily:calendar")).toBeNull();
  });

  it("still fills the days of the daily folder without a rewards source, with paper", () => {
    const rows = dailyPreviewRows({ id: "addon:daily", dataSources: [] }, "daily:calendar", {}, today)!;
    expect(rows).toHaveLength(28);
    expect(rows.every((r) => JSON.stringify(r.icon) === JSON.stringify(NO_REWARD_ICON))).toBe(true);
  });

  it("gives every day an icon when the server has no row (previewed as nobody)", () => {
    const rows = dailyPreviewRows(project, "daily:calendar", { serverRows: [] }, today)!;
    expect(rows).toHaveLength(7 + 1 + 20);
    for (const row of rows) expect(row.icon).toBeDefined();
    const todayRow = rows.find((r) => r.is_today)!;
    expect(todayRow).toMatchObject({ date: "18/11", id: "2026-11-18", is_available: true, cycle_day: 5, icon: bread.icon });
    expect(rows.find((r) => r.cycle_day === 7)?.icon).toEqual(diamond.icon);
  });

  it("shows each state of a day once in the past", () => {
    const statuses = sampleDays(7, 0, 28).map((d) => d.status);
    expect(statuses).toEqual(["before", "before", "claimed", "claimed", "claimed", "missed", "available", "available"]);
  });

  it("has no reward and the paper icon for a day before or missed", () => {
    const rows = dailyPreviewRows(project, "daily:calendar", {}, today)!;
    const missed = rows.find((r) => r.is_missed)!;
    expect(missed).toMatchObject({ cycle_day: 0, has_reward: false, icon: NO_REWARD_ICON });
  });

  it("applies the menu's past and future params", () => {
    const rows = dailyPreviewRows(project, "daily:calendar", { params: { past: "3", future: "2" } }, today)!;
    expect(rows.map((r) => r.offset)).toEqual([-3, -2, -1, 0, 1, 2]);
  });

  it("keeps the server's days, with the draft's rewards", () => {
    const server = [{ id: "x", cycle_day: 7, status: "upcoming", icon: { material: "STONE" }, has_reward: true }];
    const rows = dailyPreviewRows(project, "daily:calendar", { serverRows: server }, today)!;
    expect(rows).toHaveLength(1);
    expect(rows[0]).toMatchObject({ ...server[0], icon: diamond.icon, is_milestone: false });
  });

  it("drops the blank title the item form writes, so the item keeps its own name", () => {
    const blank = {
      dataSources: [
        {
          id: "rewards",
          shape: "config",
          value: {
            cycle_length: 7,
            default_day: { icon: { extends: "item:ci_1", title: "" } },
            days: { "7": { icon: { material: "DIAMOND", title: { en: "", fr: " " } } } },
            milestones: [{ at: 6, icon: { material: "APPLE", title: "" } }],
          },
        },
      ],
    };
    const rows = dailyPreviewRows(blank, "daily:calendar", {}, today)!;
    expect(rows.find((r) => r.is_today)?.icon).toEqual({ extends: "item:ci_1" });
    expect(rows.find((r) => r.cycle_day === 7)?.icon).toEqual({ material: "DIAMOND" });
    expect(rows.find((r) => r.streak === 6)?.milestone_icon).toEqual({ material: "APPLE" });
  });

  it("takes the icon titles of the other languages from the project's translations", () => {
    const translated = {
      defaultLang: "en",
      i18nKeys: { "sources.rewards.default_day.icon.title": { fr: "<gold>500$" } },
      dataSources: [
        { id: "rewards", shape: "config", value: { cycle_length: 7, default_day: { icon: { extends: "item:ci_1", title: "" } } } },
      ],
    };
    const rows = dailyPreviewRows(translated, "daily:calendar", {}, today)!;
    expect(rows.find((r) => r.is_today)?.icon).toEqual({ extends: "item:ci_1", title: { fr: "<gold>500$" } });
  });

  it("uses the sample status unless the server's row is loaded", () => {
    const notLoaded = dailyPreviewRows(project, "daily:status", { serverRows: [{ loaded: false, streak: 0 }] })!;
    expect(notLoaded[0]).toMatchObject({ loaded: true, streak: 4, cycle_length: 7 });
    const loaded = dailyPreviewRows(project, "daily:status", { serverRows: [{ loaded: true, streak: 9 }] })!;
    expect(loaded[0]).toMatchObject({ streak: 9, cycle_length: 7 });
  });
});

describe("the milestones preview", () => {
  const star = { material: "NETHER_STAR" };
  const withMilestones = {
    dataSources: [
      {
        id: "rewards",
        shape: "config",
        value: { cycle_length: 7, default_day: bread, milestones: [{ at: 6, icon: star }, { every: 3 }] },
      },
    ],
  };

  it("marks the sample days that reach a milestone, with its icon", () => {
    const rows = dailyPreviewRows(withMilestones, "daily:calendar", { params: { past: "7", future: "4" } }, today)!;
    const marked = rows.filter((r) => r.is_milestone).map((r) => r.streak);
    expect(marked).toEqual([3, 6, 9]);
    expect(rows.find((r) => r.streak === 6)).toMatchObject({ milestone_count: 2, milestone_icon: star });
    expect(rows.find((r) => r.streak === 9)).toMatchObject({ milestone_count: 1, milestone_icon: bread.icon });
  });

  it("lists the sample player's next milestones", () => {
    const rows = dailyPreviewRows(withMilestones, "daily:milestones", { params: { count: "4" } }, today)!;
    expect(rows.map((r) => r.id)).toEqual(["6-1", "6-2", "9-2", "12-2"]);
    expect(rows[0]).toMatchObject({ streak: 6, offset: 1, date: "19/11", repeats: false, icon: star });
    expect(rows[1]).toMatchObject({ repeats: true, every: 3, icon: NO_REWARD_ICON });
  });

  it("gives the next milestone in the sample status", () => {
    const status = dailyPreviewRows(withMilestones, "daily:status", {})!;
    expect(status[0]).toMatchObject({ has_milestones: true, next_milestone: 6, next_milestone_in: 1 });
    const none = dailyPreviewRows(project, "daily:status", {})!;
    expect(none[0]).toMatchObject({ has_milestones: false, next_milestone: 0 });
  });

  it("lays the days out as the Rewards grid with week: 7", () => {
    const rows = dailyPreviewRows(project, "daily:calendar", { params: { past: "7", future: "20", week: "7" } }, today)!;
    expect(rows).toHaveLength(28);
    // Today is day 5 of the cycle: the fifth column of its row.
    expect(rows.findIndex((r) => r.is_today) % 7).toBe(4);
    // Every day 7 of the cycle on the last column.
    for (const [i, r] of rows.entries()) if (r.cycle_day === 7) expect(i % 7).toBe(6);
    expect(weekShift(1, 7)).toBe(0);
    expect(weekShift(14, 7)).toBe(6);
    expect(weekShift(5, 0)).toBe(0);
  });
});
