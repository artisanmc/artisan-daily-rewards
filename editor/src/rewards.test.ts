import { describe, expect, it } from "vitest";
import {
  DEFAULT_CYCLE_LENGTH,
  cells,
  copyOf,
  cycleLength,
  daysOutside,
  detachTexts,
  iconCount,
  iconMaterial,
  iconTitle,
  withDay,
} from "./rewards";

const bread = { icon: { extends: "BREAD", count: 8 }, actions: [{ give_item: { material: "BREAD" } }] };
const diamond = { icon: { material: "DIAMOND", count: 3 }, actions: [] };

describe("the rewards grid", () => {
  it("shows every day of the cycle, own days first, the default for the others", () => {
    const grid = cells({ cycle_length: 7, days: { "7": diamond }, default_day: bread });
    expect(grid).toHaveLength(7);
    expect(grid[6]).toEqual({ day: 7, own: true, reward: diamond });
    expect(grid[0]).toEqual({ day: 1, own: false, reward: bread });
  });

  it("falls back to the plugin's cycle when the length is missing or wrong", () => {
    expect(cycleLength({})).toBe(DEFAULT_CYCLE_LENGTH);
    expect(cycleLength({ cycle_length: 0 })).toBe(DEFAULT_CYCLE_LENGTH);
    expect(cycleLength({ cycle_length: "14" })).toBe(14);
    expect(cells({ cycle_length: 3 })[0].reward).toBeUndefined();
  });

  it("flags the days past the cycle", () => {
    expect(daysOutside({ cycle_length: 7, days: { 3: {}, 14: {}, 9: {}, x: {} } })).toEqual([9, 14]);
  });

  it("sets and removes a day, keys in day order", () => {
    const value = { days: { "14": diamond, "7": diamond } };
    expect(Object.keys(withDay(value, 3, bread))).toEqual(["3", "7", "14"]);
    expect(withDay(value, 14, undefined)).toEqual({ "7": diamond });
  });

  it("copies a reward without sharing its objects", () => {
    const copy = copyOf(bread);
    expect(copy).toEqual(bread);
    expect(copy.icon).not.toBe(bread.icon);
  });

  it("reads an icon as the editor or the file holds it", () => {
    expect(iconMaterial(bread.icon)).toBe("BREAD");
    expect(iconMaterial(diamond.icon)).toBe("DIAMOND");
    expect(iconMaterial("EMERALD")).toBe("EMERALD");
    expect(iconCount(bread.icon)).toBe(8);
    expect(iconCount("EMERALD")).toBe(1);
  });

  it("reads a title as a literal, a Project text or a map by language", () => {
    const text = (key: string) => (key === "k" ? "<gold>Or" : "");
    expect(iconTitle({ title: "<white>Pain" }, "fr", text)).toBe("<white>Pain");
    expect(iconTitle({ title: { key: "k" } }, "fr", text)).toBe("<gold>Or");
    expect(iconTitle({ title: { en: "Bread", fr: "Pain" } }, "fr", text)).toBe("Pain");
    expect(iconTitle({ title: { en: "Bread" } }, "fr", text)).toBe("Bread");
    expect(iconTitle({}, "fr", text)).toBe("");
  });
});

describe("a day made from the other days", () => {
  it("gets texts of its own", () => {
    const reward = { icon: { extends: "BREAD", title: { key: "t" }, lore: { key: "l" } }, actions: [] };
    const copy = detachTexts(reward, (key) => (key === "t" ? "<white>Pain" : "Miam"));
    expect(copy).toEqual({ icon: { extends: "BREAD", title: "<white>Pain", lore: "Miam" }, actions: [] });
    expect(reward.icon.title).toEqual({ key: "t" });
    expect(detachTexts(undefined, () => "")).toBeUndefined();
  });
});
