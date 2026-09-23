# NadamuAuth

Lightweight, modern, and high-performance authentication plugin for the **Velocity** proxy server (Minecraft).

Designed specifically for hybrid (online/offline) networks, offering seamless player onboarding, flexible localization, and pure Java architecture with zero external database dependencies.

---

## Key Features

- **Guest-First Non-Intrusive Auth (Variant A)**:
  Unregistered players are routed straight to the lobby to explore and receive gentle periodic reminders to `/register`.
- **Ultra-Lightweight NanoLimbo Quarantine**:
  Registered players requiring password authentication are quarantined on an ultra-lightweight NanoLimbo server with strict chat and command isolation until they authenticate.
- **Smart Forced-Hosts & Routing**:
  Preserves players' intended destination servers (`forced-hosts`) across authentication. Supports single-server authentication mode (`authServer == lobbyServer`) and failover immunity for players returning from crashed backends.
- **Safe `/premium` Mojang Verification**:
  Player-initiated Mojang license linking with a 5-minute / 2-attempt window. Direct linking for new guests without requiring a password. Confirmed premium accounts authenticate instantly and cryptographically via Mojang without passwords.
- **Persistent IP-Based Fast Reconnection**:
  Database-backed IP sessions allow password players to reconnect seamlessly without re-typing their password (configurable timeout, 24h default). Survives Velocity proxy restarts.
- **Multi-Language Localization (i18n)**:
  - Default Russian (`languages/ru.yml`) and English (`languages/en.yml`) dictionaries with full MiniMessage support.
  - Automatic detection of player client locale (`player.getPlayerSettings().getLocale()`).
  - Extensible: add arbitrary language dictionaries (e.g., `de.yml`, `es.yml`).
  - Interactive chat buttons (click-to-suggest `/r `, click-to-run `/premium confirm`).
- **Embedded Pure Java Database**:
  Powered by an embedded H2 database with HikariCP connection pooling — zero external database setup and zero native C++ binaries required.
- **Enterprise Security**:
  - BCrypt password hashing (cost factor 12).
  - Sliding-window rate limiting to prevent brute-force attacks.
  - Extensible `AuthService` interface ready for external Identity Providers (e.g., Authentik OAuth 2.0 Device Flow).

---

## Commands & Permissions

| Command | Aliases | Permission | Description |
| :--- | :--- | :--- | :--- |
| `/login <password>` | `/l` | None | Log in to a registered account |
| `/register <password> <confirm>` | `/r`, `/reg` | None | Register a new password account |
| `/changepassword <old> <new>` | None | None | Change existing password |
| `/premium [confirm]` | None | None | Link or verify official Mojang license |
| `/auth reload` | `/nauth reload` | `nadamuauth.admin` | Reload `config.yml` and all language files |
| `/auth unregister <player>` | `/nauth unregister` | `nadamuauth.admin` | Remove a player's account from the database |
| `/auth setpremium <player> <true\|false>` | `/nauth setpremium` | `nadamuauth.admin` | Set a player's Mojang license status |

---

## Quickstart

1. Place `NadamuAuth-<version>.jar` into your Velocity proxy's `plugins/` directory.
2. Ensure `online-mode = false` in your `velocity.toml` (hybrid proxy setup).
3. Ensure your `limbo` and `lobby` servers are registered in `velocity.toml`.
4. Start Velocity to generate default configuration and language files at `plugins/nadamu-auth/`.
5. Adjust settings in `config.yml` or message files in `languages/` as desired.

For detailed guides and architecture documentation, see:
- [System Architecture Overview](docs/architecture/overview.md)
- [Setup & Deployment Guide (English)](docs/en/setup-guide.md)
- [Setup & Deployment Guide (Russian)](docs/ru/setup-guide.md)
