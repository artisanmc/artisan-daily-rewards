# ArtisanDailyRewards

Daily rewards for Paper servers, built on Artisan: each day a player comes counts for their
streak and earns a reward they claim, then or later; they keep a streak, earn bigger rewards as the streak grows, loop through a reward cycle, and can save a
broken streak with jokers or a paid restore.

Requires Paper 1.21+ (Folia supported) and **Artisan with API 2.17.0 or later**. Artisan provides
the database (H2, MySQL or MariaDB, set in `plugins/Artisan/environment.yml`), the action engine
that pays the rewards, the menus and the editor.

**Status:** the daily claim, the streak, the jokers, one reward per day of the cycle and the
calendar menu work. The base reward, the tiers, the milestones, the paid restore, the `daily:*`
actions and the placeholders come next.

## Calendar

Artisan keeps these files in `plugins/Artisan/modules/daily/` (a system template: a missing file
is written again, an edited one stays yours until Reset in the editor):

- `data/calendar.yml`: reset time, cycle length, auto-claim, jokers, and the rewards. `days` is
  keyed by the day of the cycle (1 to `cycle_length`), `default_day` covers the others. Each
  reward has an `icon` (the item the menu shows) and `actions` (run at the claim, with
  `{streak}`, `{cycle_day}` and `{cycle_length}`). Edited in the editor's **Calendar** screen.
- `menus/calendar.yml`: what `/daily` opens. The last 7 days as they went (claimed, waiting to be
  claimed, missed), today (a click claims everything that waits), and the next 20 days with the
  reward each one pays if the player comes every day.

The menu reads two data sources:

| Source | Rows | Fields |
|---|---|---|
| `daily:calendar` | one per game day; params `past` (7, at most 35) and `future` (20) | `id`, `date`, `offset`, `status` (`before`, `claimed`, `missed`, `available`, `upcoming`), `is_today`, `is_claimed`, `is_missed`, `is_before`, `is_available`, `is_upcoming`, `cycle_day`, `has_reward`, `icon` |
| `daily:status` | one | `loaded`, `streak`, `longest`, `total`, `freezes`, `freeze_max`, `claimed_today`, `unclaimed`, `can_claim`, `at_risk`, `broken`, `lost`, `cycle_day`, `cycle_length`, `until_reset` |

`available` is any day whose reward waits: today, or a day before when the player was there
without claiming.

## Days and claims

A day counts for the streak when the player is there: they join (or are online at the reset)
and play `min_playtime_minutes`. Claiming is separate. The reward of a counted day waits, with
the day of the cycle and the streak of that day, until the player claims: `/daily claim` (or the
menu, or auto-claim) pays every reward that waits, however old. A day the player was not there is
missed: the jokers may keep the streak, its reward is lost.

The first time a player counts in a game day, the `unlocked` message (in `lang/`, a click opens
`/daily`) tells them the reward is unlocked; `join_reminder: false` turns it off, auto-claim
claims instead.

A claim takes its delivery in the database before the actions run, so two servers never pay one
reward twice. A player who leaves before the reward is handed out gets it at their next join.

`/daily status` still prints the streak in chat.

## Build

```bash
./gradlew build   # → build/libs/ArtisanDailyRewards-<version>.jar
```

`artisan-core-api` comes from `repo.artisanmc.net`. Until the version in `gradle.properties` is
released there, publish it from Artisan's `v2` branch:

```bash
cd <artisan>/plugin && ./gradlew :artisan-api:publishToMavenLocal -PapiVersion=2.17.0
```
