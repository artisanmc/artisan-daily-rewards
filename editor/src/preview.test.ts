import { describe, expect, it } from "vitest";
import { NO_REWARD_ICON, dailyPreviewRows, sampleDays } from "./preview";

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
    expect(rows).toEqual([{ ...server[0], icon: diamond.icon }]);
  });

  it("uses the sample status unless the server's row is loaded", () => {
    const notLoaded = dailyPreviewRows(project, "daily:status", { serverRows: [{ loaded: false, streak: 0 }] })!;
    expect(notLoaded[0]).toMatchObject({ loaded: true, streak: 4, cycle_length: 7 });
    const loaded = dailyPreviewRows(project, "daily:status", { serverRows: [{ loaded: true, streak: 9 }] })!;
    expect(loaded[0]).toMatchObject({ streak: 9, cycle_length: 7 });
  });
});
