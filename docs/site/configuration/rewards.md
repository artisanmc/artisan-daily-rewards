---
title: rewards.yml
description: The length of the reward cycle, the reward of each day and the streak milestones.
sidebar:
  order: 2
---

`plugins/Artisan/modules/daily/data/rewards.yml` holds the cycle. Edit it in the editor (the **daily**
folder, **Rewards**): a grid of the cycle, the days with their own reward highlighted, the others showing
`default_day` faded. A click on a day edits it, gives it its own reward, or sends it back to `default_day`.
Under the grid, the streak milestones: a click on one edits it beside the grid, the buttons add one.

## The shipped file

```yaml
cycle_length: 28         # days before the rewards start again at day 1
default_day:             # the reward of a day not listed under days
  icon:
    material: BREAD
    count: 8
    title: { en: "<white>8 bread", fr: "<white>8 pains" }
  actions:
    - give_item: { material: BREAD, count: 8 }
days:
  7:
    icon:
      material: IRON_INGOT
      count: 8
      title: { en: "<white>8 iron ingots", fr: "<white>8 lingots de fer" }
    actions:
      - give_item: { material: IRON_INGOT, count: 8 }
  14:
    icon: { material: GOLD_INGOT, count: 8, title: { en: "<gold>8 gold ingots" } }
    actions:
      - give_item: { material: GOLD_INGOT, count: 8 }
  21:
    icon: { material: EMERALD, count: 6, title: { en: "<green>6 emeralds" } }
    actions:
      - give_item: { material: EMERALD, count: 6 }
  28:
    icon: { material: DIAMOND, count: 3, title: { en: "<aqua>3 diamonds" } }
    actions:
      - give_item: { material: DIAMOND, count: 3 }
milestones:              # bonuses of the streak, on top of the day's reward
  - at: 30               # once per streak, the day the streak reaches 30
    icon: { material: ENCHANTED_GOLDEN_APPLE, title: { en: "<light_purple>30-day streak: an enchanted golden apple" } }
    actions:
      - give_item: { material: ENCHANTED_GOLDEN_APPLE, count: 1 }
  - every: 50            # again and again: at 50, 100, 150… of streak
    icon: { material: NETHERITE_INGOT, title: { en: "<dark_purple>Every 50 days of streak: a netherite ingot" } }
    actions:
      - give_item: { material: NETHERITE_INGOT, count: 1 }
```

## Keys

| Key | Type | Default | Effect |
|---|---|---|---|
| `cycle_length` | integer ≥ 1 | `28` | Days of the cycle. After the last, the next day is day 1 again. |
| `default_day` | reward | 8 bread | The reward of every day not under `days`. |
| `days.<n>` | reward | days 7, 14, 21, 28 | The reward of day `n` of the cycle, 1 to `cycle_length`. |
| `<reward>.icon` | item | | What the calendar shows: `material`, `count`, `title` by language. |
| `<reward>.actions` | action chain | | Run at the claim. `{streak}`, `{cycle_day}` and `{cycle_length}` hold the values of the day claimed. |
| `milestones` | list | 30, then every 50 | Bonuses of the streak, paid on top of the day's reward. |
| `milestones[].at` | integer ≥ 1 | | Paid once per streak, the day the streak reaches it. |
| `milestones[].every` | integer ≥ 1 | | Paid again and again: each time the streak reaches `from`, `from + every`, `from + 2 × every`… |
| `milestones[].from` | integer ≥ 1 | `every` | With `every` only: the first streak it pays. |
| `milestones[].icon`, `.actions` | item, action chain | | As a day's reward; shown in the milestones menu, run with the claim of the day that reaches it. |

## Milestones

The cycle pays one reward a day and starts again at day 1 after `cycle_length` days, without touching
the streak. Milestones follow the streak itself: a day whose streak reaches one pays it **on top of** the
day's reward, with the same claim (`{streak}` is that day's streak). A milestone has `at` or `every`,
never both:

- `at: 30`: once per streak, the day the streak reaches 30;
- `every: 50`: at 50, 100, 150…; with `from: 100`: at 100, 150, 200…

Everything a day reaches is paid: at streak 100, `every: 50` and an `at: 100` both pay. A milestone is
paid once per streak: the plugin keeps the highest streak it paid (`milestone_mark`), and a later day
at or below it pays nothing, even if an admin lowers the streak. A break starts the streak again,
and the milestones with it.

:::caution[End with an every milestone]
A one-time milestone pays again after a break. When no milestone repeats, a player past the last one
earns more by breaking their streak and climbing back than by going on. Keep at least one `every`
milestone, as the shipped file does; the Rewards screen warns when none repeats.
:::

### A cycle or milestones?

`every` covers part of what a cycle does, but they do not overlap. The cycle gives **every** day a
reward and shapes a week or a month (a better reward each 7th day, the biggest at the end); it loops
forever. Milestones are rare and follow the whole streak: a big bonus at 30 days, then every 50. Use
the cycle for the daily rhythm, milestones for the long run.

## The calendar menu

`menus/calendar.yml` shows the last 7 days as they went (claimed, waiting, missed), today (a click claims
everything that waits) and the next 104 days with the reward each pays if the player comes every day,
28 days a page (arrows at the bottom: `pagination_prev` / `pagination_next` on the `days` element).
A day that reaches a milestone glows, shows the milestone's icon (to come) and says
`+ streak bonus`. The beacon at the bottom left gives the next milestone and opens `menus/milestones.yml`:
the next 112 milestones the player reaches if they come every day, 28 a page, with the day each one falls
on and whether it repeats.

The menus read three data sources, which your own menus can read too:

| Source | Rows | Fields |
|---|---|---|
| `daily:calendar` | one per game day; params `past` (7, at most 35) and `future` (20) | `id`, `date`, `offset`, `status` (`before`, `claimed`, `missed`, `available`, `upcoming`), `is_today`, `is_claimed`, `is_missed`, `is_before`, `is_available`, `is_upcoming`, `cycle_day`, `has_reward`, `icon`, `streak`, `is_milestone`, `milestone_count`, `milestone_icon` |
| `daily:status` | one | `loaded`, `streak`, `longest`, `total`, `freezes`, `freeze_max`, `claimed_today`, `unclaimed`, `can_claim`, `at_risk`, `broken`, `lost`, `cycle_day`, `cycle_length`, `until_reset`, `next_milestone` (0 when none is left), `next_milestone_in` (days, -1 when none is left) |
| `daily:milestones` | one per coming milestone, the next ones the viewer reaches coming every day; param `count` (28, at most 200) | `id`, `streak`, `offset`, `date`, `is_today`, `repeats`, `every`, `icon` |
