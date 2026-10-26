# ArtisanDailyRewards

Daily rewards for Paper servers, built on Artisan: players claim a reward once per day, keep a
streak, earn bigger rewards as the streak grows, loop through a reward cycle, and can save a
broken streak with jokers or a paid restore.

Requires Paper 1.21+ (Folia supported) and **Artisan with API 2.10.0 or later**. Artisan provides
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
cd <artisan>/plugin && ./gradlew :artisan-api:publishToMavenLocal -PapiVersion=2.10.0
```

## Release

Push a tag `v<version>` (`git tag v1.0.3 && git push origin v1.0.3`). `.github/workflows/release.yml`
builds `ArtisanDailyRewards-<version>.jar`, waits for the `release` environment to be approved, signs
the jar with `scripts/sign-release.sh` and publishes it with `release.json` (`{plugin, version, sha256,
signature}`) on the tag's GitHub Release. Artisan checks that signature against the public key it pins
before it stages an update in `plugins/update/`.

One-time setup: a `release` environment (Settings → Environments) with a required reviewer and the
secret `ARTISAN_SIGNING_KEY`, the Ed25519 private key in PEM (`openssl genpkey -algorithm ed25519`).
