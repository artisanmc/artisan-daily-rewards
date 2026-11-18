---
title: Money and a command as a reward
description: Pay money and run a console command, using the streak in the reward.
sidebar:
  order: 3
---

Every day pays 100 coins and tells the player their streak; day 28 also gives a crate key through your
crates plugin.

```yaml
# plugins/Artisan/modules/daily/data/rewards.yml
default_day:
  icon: { material: SUNFLOWER, count: 1, title: { en: "<yellow>100 coins" } }
  actions:
    - give_money: 100
    - send_message: { text: "<gray>Day {cycle_day}/{cycle_length}, streak {streak}." }
days:
  28:
    icon: { material: TRIPWIRE_HOOK, count: 1, title: { en: "<light_purple>A legendary key" } }
    actions:
      - run_command: { command: "crates key give {player} legendary 1", as: console }
```

The command runs as the console; check the exact syntax of your crates plugin.
