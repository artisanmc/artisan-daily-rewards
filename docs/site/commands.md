---
title: Commands and permissions
description: /dailyrewards, /dailyadmin and the permissions.
sidebar:
  order: 3
---

| Command | Permission | Effect | Example |
|---|---|---|---|
| `/dailyrewards` | `daily.use` (everyone) | Opens the calendar. | `/dailyrewards` |
| `/dailyrewards claim` | `daily.use` | Claims every reward that waits. | `/dailyrewards claim` |
| `/dailyrewards status` | `daily.use` | Your streak, best streak and jokers, in chat. | `/dailyrewards status` |
| `/dailyrewards autoclaim [on\|off]` | `daily.use` | Claim automatically at join; only when the settings say `auto_claim: player`. Without an argument, toggles. | `/dailyrewards autoclaim on` |
| `/dailyadmin info <player>` | `daily.admin` (op) | A player's streak, jokers and ledger. | `/dailyadmin info Steve` |
| `/dailyadmin set <player> <streak\|longest\|freezes> <value>` | `daily.admin` | Corrects a counter. `streak` restores a streak that holds: the player counts as there yesterday (or today, if today already counts), so tomorrow continues it, even after a break; the longest streak rises to it; no milestone at or below it is paid (again). `streak 0` starts over. | `/dailyadmin set Steve streak 40` |
| `/dailyadmin give <player> freezes <n>` | `daily.admin` | Gives `n` jokers on top of those held, logged in the ledger (`freeze_given`). It can go over `freeze.max_stock`; the player then earns none until back under it. To remove some: `set … freezes`. | `/dailyadmin give Steve freezes 2` |
| `/dailyadmin day [+n\|-n\|reset]` | `daily.admin` | Shifts this server's game day, to test the cycle without waiting. | `/dailyadmin day +1` |

| Permission | Default | Allows |
|---|---|---|
| `daily.use` | everyone | `/dailyrewards` and its sub-commands |
| `daily.admin` | op | `/dailyadmin` |
| `daily.restore`, `daily.restore.free` | everyone, nobody | Declared for the paid restore of a broken streak, which is not available yet |

The commands are declared to Artisan: rename them or add aliases in the command files of
`plugins/Artisan/modules/daily/commands/`, or in the editor.
