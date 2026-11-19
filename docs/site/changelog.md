---
title: Changelog
description: The versions of Artisan Daily Rewards, newest first.
sidebar:
  order: 6
---

## 1.0.0 (unreleased)

- Requires Artisan API 1.0.0 (Artisan 1.0.0).
- Daily claim, streak, jokers, one reward per day of the cycle, auto-claim, join reminder and the
  calendar menu.
- Settings and rewards split into `data/settings.yml` and `data/rewards.yml`, edited in the editor's
  **Settings** and **Rewards** screens; an older `data/calendar.yml` is split on start.
- The calendar menu previews in the editor with a sample player (every state of a day) and the rewards
  being edited; previewed as a player, its days as they went with the draft's rewards.
- The calendar menu pages through the next 104 days, 28 a page, its rows laid out as the Rewards
  grid (days 7, 14, 21, 28 on the last column; `daily:calendar` param `week`).
- Streak milestones (`milestones` in `data/rewards.yml`): bonuses on top of the day's reward, `at` once per
  streak or `every` again and again, edited under the Rewards grid. A day that reaches one glows in the
  calendar; a new milestones menu (`menus/milestones.yml`) pages through the next ones. New data source
  `daily:milestones`, new fields in `daily:calendar` and `daily:status`. The calendar's milestones button shows only when `milestones`
  holds at least one (`daily:status` field `has_milestones`).
- `with_default_day` in `data/rewards.yml`: a day with its own reward replaces `default_day` (`false`, as
  before) or pays it too (`true`), for every day or day by day; set in the Rewards screen, a `+` marks the
  days that pay both.
- A reward or milestone icon shows its title in the player's language: the editor keeps the other
  languages in `lang/<lang>.yml` (`sources.rewards.…icon.title`), now read with the rewards. With no title
  in any language, the item keeps its own name (it showed no name). The calendar's claim button shows "Claim" or the time to the next
  reward (it stayed on "Loading…").
- `/dailyadmin set <player> streak <n>` restores a streak that holds: it continues the next day, even after a
  break, and raises the longest streak (it used to be broken again at the next day).
- Not yet: paid restore, tiers, `daily:*` actions, placeholders.
