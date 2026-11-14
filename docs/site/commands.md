---
title: Commands and permissions
description: /daily, /dailyadmin and the permissions.
sidebar:
  order: 3
---

| Command | Permission | Effect | Example |
|---|---|---|---|
| `/daily` | `daily.use` (everyone) | Opens the calendar. | `/daily` |
| `/daily claim` | `daily.use` | Claims every reward that waits. | `/daily claim` |
| `/daily status` | `daily.use` | Your streak, best streak and jokers, in chat. | `/daily status` |
| `/daily autoclaim [on\|off]` | `daily.use` | Claim automatically at join; only when the settings say `auto_claim: player`. Without an argument, toggles. | `/daily autoclaim on` |
| `/dailyadmin info <player>` | `daily.admin` (op) | A player's streak, jokers and ledger. | `/dailyadmin info Steve` |
| `/dailyadmin set <player> <streak\|longest\|freezes> <value>` | `daily.admin` | Corrects a counter. | `/dailyadmin set Steve freezes 2` |
| `/dailyadmin day [+n\|-n\|reset]` | `daily.admin` | Shifts this server's game day, to test the cycle without waiting. | `/dailyadmin day +1` |

| Permission | Default | Allows |
|---|---|---|
| `daily.use` | everyone | `/daily` and its sub-commands |
| `daily.admin` | op | `/dailyadmin` |
| `daily.restore`, `daily.restore.free` | everyone, nobody | Declared for the paid restore of a broken streak, which is not available yet |

The commands are declared to Artisan: rename them or add aliases in the command files of
`plugins/Artisan/modules/daily/commands/`, or in the editor.
