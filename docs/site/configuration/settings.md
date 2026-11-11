---
title: settings.yml
description: When the game day ends, auto-claim, playtime, the join reminder and the jokers.
sidebar:
  order: 1
---

`plugins/Artisan/modules/daily/data/settings.yml` sets how days count. Edit it in the editor (the
**daily** folder, **Settings**); a publish applies it.

```yaml
reset_time: "00:00"      # when the game day ends, server time
auto_claim: "off"        # off | on | player
min_playtime_minutes: 0  # minutes of the session before the day counts
join_reminder: true      # tell the player their reward is unlocked
freeze:
  earn_every: 7          # days of streak per joker; 0: never
  max_stock: 2           # jokers held at most
```

| Key | Type | Default | Effect |
|---|---|---|---|
| `reset_time` | `HH:MM` | `"00:00"` | When the game day ends, in the server's time zone. |
| `auto_claim` | `off`, `on`, `player` | `off` | `off`: the player claims. `on`: claimed at join and at the reset. `player`: each player chooses with `/daily autoclaim`. |
| `min_playtime_minutes` | integer ≥ 0 | `0` | Minutes of the current session before the day counts and its reward can be claimed. |
| `join_reminder` | boolean | `true` | Tell a player their reward is unlocked when the day counts, and remind them of rewards that wait. |
| `freeze.earn_every` | integer ≥ 0 | `7` | Days of streak per joker. `0`: never. |
| `freeze.max_stock` | integer ≥ 0 | `2` | Jokers a player holds at most. |

## How a day counts

A day counts for the streak when the player is there: they join (or are online at the reset) and play
`min_playtime_minutes`. Its reward then waits, with the day of the cycle and the streak of that day, until
they claim: `/daily claim`, the menu or auto-claim pays every reward that waits, however old.

A day the player was not there is missed: a joker covers it and the streak goes on; without one, the
streak breaks and that day's reward is lost.

A claim is recorded in the database before the actions run, so two servers on one database never pay one
reward twice. A player who leaves before the reward is handed out gets it at their next join.

## Messages

The messages are in the jar's `lang/en.yml` and `lang/fr.yml` (MiniMessage). Override a key in
`plugins/Artisan/modules/daily/lang/<code>.yml`; a new `<code>.yml` adds a language.
