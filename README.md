# ArtisanDailyRewards

Daily rewards for Paper servers, built on Artisan: players claim a reward once per day, keep a
streak, earn bigger rewards as the streak grows, loop through a reward cycle, and can save a
broken streak with jokers or a paid restore.

Requires Paper 1.21+ (Folia supported) and **Artisan with API 2.9.0 or later**. Artisan provides
the database (H2, MySQL or MariaDB, set in `plugins/Artisan/environment.yml`), the action engine
that pays the rewards, the menus and the editor.

**Status:** skeleton. The plugin loads, checks Artisan and claims its folder
`plugins/Artisan/modules/daily/`; nothing else yet.

## Build

```bash
./gradlew build   # → build/libs/ArtisanDailyRewards-<version>.jar
```

`artisan-core-api` comes from `repo.artisanmc.net`. Until the version in `gradle.properties` is
released there, publish it from Artisan's `v2` branch:

```bash
cd <artisan>/plugin && ./gradlew :artisan-api:publishToMavenLocal -PapiVersion=2.9.0
```
