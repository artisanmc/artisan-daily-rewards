---
title: rewards.yml
description: The length of the reward cycle and the reward of each day.
sidebar:
  order: 2
---

`plugins/Artisan/modules/daily/data/rewards.yml` holds the cycle. Edit it in the editor (the **daily**
folder, **Rewards**): a grid of the cycle, the days with their own reward highlighted, the others showing
`default_day` faded. A click on a day edits it, gives it its own reward, or sends it back to `default_day`.

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
```

## Keys

| Key | Type | Default | Effect |
|---|---|---|---|
| `cycle_length` | integer ≥ 1 | `28` | Days of the cycle. After the last, the next day is day 1 again. |
| `default_day` | reward | 8 bread | The reward of every day not under `days`. |
| `days.<n>` | reward | days 7, 14, 21, 28 | The reward of day `n` of the cycle, 1 to `cycle_length`. |
| `<reward>.icon` | item | | What the calendar shows: `material`, `count`, `title` by language. |
| `<reward>.actions` | action chain | | Run at the claim. `{streak}`, `{cycle_day}` and `{cycle_length}` hold the values of the day claimed. |

## The calendar menu

`menus/calendar.yml` shows the last 7 days as they went (claimed, waiting, missed), today (a click claims
everything that waits) and the next 20 days with the reward each pays if the player comes every day.
It reads two data sources, which your own menus can read too:

| Source | Rows | Fields |
|---|---|---|
| `daily:calendar` | one per game day; params `past` (7, at most 35) and `future` (20) | `id`, `date`, `offset`, `status` (`before`, `claimed`, `missed`, `available`, `upcoming`), `is_today`, `is_claimed`, `is_missed`, `is_before`, `is_available`, `is_upcoming`, `cycle_day`, `has_reward`, `icon` |
| `daily:status` | one | `loaded`, `streak`, `longest`, `total`, `freezes`, `freeze_max`, `claimed_today`, `unclaimed`, `can_claim`, `at_risk`, `broken`, `lost`, `cycle_day`, `cycle_length`, `until_reset` |
