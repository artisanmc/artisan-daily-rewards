---
title: Troubleshooting
description: What a message means and how to fix it.
sidebar:
  order: 5
---

| Symptom | Cause | Fix |
|---|---|---|
| The plugin disables itself: `Artisan is required but its API is not available` or `Artisan API … is too old` | Artisan is missing or older than API 1.0.0. | Install or update Artisan. |
| `Play … more to claim today's reward.` | `min_playtime_minutes` is not reached in this session. | Wait, or lower the setting. |
| `Your streak changed at the same time elsewhere, please try again.` | Two servers on one database claimed at the same moment. | Claim again: one of the two claims went through. |
| `Daily rewards are paused on this server, ask an administrator.` | This server's time zone differs from the one recorded in the shared database: claiming would pay a day twice. The console names both zones. | Run every server on the shared database in the same time zone (`-Duser.timezone=…`), then restart. |
| The day does not change at midnight | `reset_time` is in the server's time zone, shown on start (`Enabled in …`). | Set `reset_time` for that zone, or change the server's zone. |
| A reward was missed | The player was not there that day and had no joker. | `/dailyadmin set <player> streak <n>` restores a counter; the day's reward is not paid back. |
