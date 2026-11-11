---
title: Play before claiming
description: Count a day only after 15 minutes of play, and claim automatically.
sidebar:
  order: 2
---

A day counts after 15 minutes in the session, and its reward is claimed for the player at once.

```yaml
# plugins/Artisan/modules/daily/data/settings.yml
min_playtime_minutes: 15
auto_claim: "on"
join_reminder: true
```

Before the 15 minutes, `/daily claim` answers how long is left. With `auto_claim: "on"`, the reward is
also claimed at the reset for players online then.
