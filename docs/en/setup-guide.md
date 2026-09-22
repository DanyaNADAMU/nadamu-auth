# Setup & Deployment Guide

## Prerequisites
- Velocity Proxy 4.0.0 or higher.
- Java 25 runtime.
- NanoLimbo instance running and registered in Velocity.
- At least one backend game server (e.g., Paper Lobby).

## Installation
1. Build the plugin jar using `./gradlew shadowJar`.
2. Copy `build/libs/NadamuAuth-1.0-SNAPSHOT.jar` into your Velocity proxy's `plugins/` folder.
3. Start the proxy to generate default configuration files at `plugins/nadamu-auth/`:
   - `config.yml`
   - `messages.yml`

## Velocity Configuration (`velocity.toml`)
Ensure your servers are declared:
```toml
[servers]
limbo = "127.0.0.1:25566"
lobby = "127.0.0.1:25567"

try = [
  "lobby"
]
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
  session-timeout-minutes: 1440
  login-timeout-seconds: 60

guest:
  allow-guest: true
  reminder-interval-seconds: 60

premium:
  enabled: true
  verification-timeout-minutes: 5
  max-failed-attempts: 2
```

## Permissions
- `nadamuauth.admin` — grants access to `/auth reload`, `/auth unregister <player>`, and `/auth setpremium <player> <true|false>`.
