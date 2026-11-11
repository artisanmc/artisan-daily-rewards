---
title: A weekly cycle with a big day 7
description: Seven days that loop, a small daily reward and a bigger one at the end of each week.
sidebar:
  order: 1
---

Days 1 to 6 pay 500 coins, day 7 pays a diamond block, then the week starts again.

```yaml
# plugins/Artisan/modules/daily/data/rewards.yml
cycle_length: 7
default_day:
  icon: { material: GOLD_NUGGET, count: 1, title: { en: "<yellow>500 coins" } }
  actions:
    - give_money: 500
days:
  7:
    icon: { material: DIAMOND_BLOCK, count: 1, title: { en: "<aqua>A diamond block" } }
    actions:
      - give_item: { material: DIAMOND_BLOCK, count: 1 }
      - broadcast: { text: "<aqua>{player} completed a week of daily rewards!" }
```

`give_money` needs Vault and an economy plugin. Test the week without waiting: `/dailyadmin day +1`, then
`/daily claim`, seven times; `/dailyadmin day reset` puts the day back.
