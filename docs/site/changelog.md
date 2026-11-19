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
- The calendar menu pages through the next 104 days, 28 a page.
- Streak milestones (`milestones` in `data/rewards.yml`): bonuses on top of the day's reward, `at` once per
  streak or `every` again and again, edited under the Rewards grid. A day that reaches one glows in the
  calendar; a new milestones menu (`menus/milestones.yml`) pages through the next ones. New data source
  `daily:milestones`, new fields in `daily:calendar` and `daily:status`.
- `with_default_day` in `data/rewards.yml`: a day with its own reward replaces `default_day` (`false`, as
  before) or pays it too (`true`), for every day or day by day; set in the Rewards screen, a `+` marks the
  days that pay both.
- Not yet: paid restore, tiers, `daily:*` actions, placeholders.
