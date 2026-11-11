---
title: Artisan Daily Rewards
description: Daily rewards with a streak, a reward cycle and jokers, edited in Artisan.
sidebar:
  order: 0
---

Artisan Daily Rewards pays players for coming back: each day they play counts for their streak and
unlocks the reward of that day of the cycle, which they claim then or later. Jokers keep a streak alive
over a missed day.

What works today: the daily claim, the streak, the jokers, one reward per day of the cycle, auto-claim and
the calendar menu. The paid restore of a broken streak, tiers, milestones, `daily:*` actions and
placeholders are not there yet.

## Requirements

| | |
|---|---|
| Server | Paper 1.21 or newer, Folia supported |
| Java | 21 |
| Artisan | API 1.0.0 or newer (Artisan 1.0.0) |

Artisan provides the database (H2, MySQL or MariaDB, set in `plugins/Artisan/environment.yml`), the
action engine that pays the rewards, the menu and the editor.

## Where to go next

- [Install it](install/).
- [Settings](configuration/settings/) (reset time, auto-claim, jokers) and
  [rewards](configuration/rewards/) (the cycle).
- [Commands and permissions](commands/).
- Worked cases: [a weekly cycle](recipes/weekly-cycle/), [play before claiming](recipes/min-playtime/),
  [money and a command as a reward](recipes/money-reward/).
