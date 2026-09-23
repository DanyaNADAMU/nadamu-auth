# Setup & Deployment Guide

## Prerequisites
- Velocity Proxy 4.0.0 or higher.
- Java 25 runtime.
- NanoLimbo instance running and registered in Velocity.
- At least one backend game server (e.g., Paper Lobby).

## Installation
1. Build the plugin jar using `./gradlew shadowJar` or download the latest release jar `NadamuAuth-<version>.jar`.
2. Copy the jar into your Velocity proxy's `plugins/` directory.
3. Start the proxy to generate default configuration and language dictionaries at `plugins/nadamu-auth/`:
   - `config.yml`
   - `languages/ru.yml`
   - `languages/en.yml`

## Velocity Configuration (`velocity.toml`)

In hybrid proxy mode, configure `velocity.toml`:
```toml
# Must be false so Velocity allows both cracked and licensed players
online-mode = false

[servers]
limbo = "127.0.0.1:25566"
lobby = "127.0.0.1:25567"
pvp = "127.0.0.1:25568"

try = [
  "lobby"
]

[forced-hosts]
"pvp.example.com" = [ "pvp" ]
```

## Plugin Configuration (`config.yml`)
```yaml
servers:
  auth-server: "limbo"
  lobby-server: "lobby"

database:
  type: "H2"
  file-name: "nadamu_auth"
  pool-size: 10

security:
  min-password-length: 6
  max-password-length: 64
  max-login-attempts: 5
  lockout-minutes: 10
  # Session validity in minutes (1440 = 24 hours, 0 = disabled)
  session-timeout-minutes: 1440
  login-timeout-seconds: 60

guest:
  allow-guest: true
  reminder-interval-seconds: 60

premium:
  enabled: true
  verification-timeout-minutes: 5
  max-failed-attempts: 2

localization:
  # Default language fallback if client language is not found in languages/
  default-language: "ru"
```

## Localization (i18n)

Languages are located in `plugins/nadamu-auth/languages/`.
- Default files `ru.yml` and `en.yml` are generated automatically.
- Player language is auto-detected from their Minecraft client settings (`player.getPlayerSettings().getLocale()`).
- Administrators can edit messages or add new `.yml` language files (e.g. `de.yml`, `es.yml`).
- Run `/auth reload` to reload `config.yml` and all language files without restarting the proxy.

## Permissions
- `nadamuauth.admin` — grants access to:
  * `/auth reload` (`/nauth reload`)
  * `/auth unregister <player>` (`/nauth unregister`)
  * `/auth setpremium <player> <true|false>` (`/nauth setpremium`)
