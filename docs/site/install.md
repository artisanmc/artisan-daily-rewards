---
title: Install
description: Put the jar next to Artisan, start the server, and claim the first reward.
sidebar:
  order: 1
---

1. Install [Artisan](../../artisan/getting-started/install-admin/) 1.0.0 or newer.
2. Drop `ArtisanDailyRewards-<version>.jar` into `plugins/`.
3. Start the server. The console names the time zone of the game day:

   ```text
   [ArtisanDailyRewards] Enabled in Europe/Paris (Artisan API 1.0.0).
   ```

4. Join, type `/daily`: the calendar opens, today's reward waits. A click on today claims it.

## What the first start writes

Artisan keeps these files in `plugins/Artisan/modules/daily/`:

| Path | What |
|---|---|
| `data/settings.yml` | [Settings](../configuration/settings/): reset time, auto-claim, playtime, reminder, jokers |
| `data/rewards.yml` | [Rewards](../configuration/rewards/): the cycle and the reward of each day |
| `menus/calendar.yml` | What `/daily` opens |
| `lang/<code>.yml` | Your overrides of the messages |

They are system elements: a missing file is written again, an untouched one follows each new version,
an edited one stays yours until **Reset** in the editor.

## Coming from an older version

Before version 2 of these files, everything was in `data/calendar.yml`. At start, an edited
`data/calendar.yml` is split into `settings.yml` and `rewards.yml`; the old one is copied to
`plugins/ArtisanDailyRewards/calendar-<date>.yml`, then removed. When both new files already exist, it
is left as is and no longer read.
