# NadamuAuth

Lightweight, modern authentication and player management plugin for the **Velocity** proxy server (Minecraft).

## Features
- **Guest-First Non-Intrusive Auth (Variant A)**: Unregistered players are sent straight to the game lobby and receive gentle reminders to `/register`.
- **NanoLimbo Integration**: Registered players are quarantined on an ultra-lightweight NanoLimbo server until they authenticate with `/login`.
- **Safe `/premium` Verification**: Player-initiated Mojang license verification with a 5-minute TTL and a 2-failed-attempt fallback to protect against accidental lockouts.
- **Embedded Pure Java Database**: Powered by H2 and HikariCP connection pooling, requiring no external database services or native JNI binaries.
- **Robust Security**:
  - BCrypt password hashing (cost factor 12).
  - Brute-force rate limiting with temporary lockouts per IP and username.
  - Configurable IP-based session persistence (24h default).
- **Extensible Architecture**: Ready for external Identity Providers such as **Authentik** via the `AuthService` interface.

## Commands
| Command | Alias | Permission | Description |
| :--- | :--- | :--- | :--- |
| `/login <password>` | `/l` | None | Authenticate on the auth server |
| `/register <password> <confirm>` | `/reg` | None | Register a new account |
| `/changepassword <old> <new>` | None | None | Change current password |
| `/premium [confirm]` | None | None | Link official Mojang license |
| `/auth reload` | `/nauth reload` | `nadamuauth.admin` | Reload config and messages |
| `/auth unregister <player>` | `/nauth unregister` | `nadamuauth.admin` | Delete a player's account |
| `/auth setpremium <player> <val>` | `/nauth setpremium` | `nadamuauth.admin` | Set Mojang license status |

## Quickstart
1. Place `NadamuAuth-1.0-SNAPSHOT.jar` into the `plugins/` directory of your Velocity proxy.
2. Ensure NanoLimbo and Lobby servers are registered in `velocity.toml`.
3. Configure `config.yml` in `plugins/nadamu-auth/` if your server names differ from `limbo` and `lobby`.
4. Start or restart Velocity.

For detailed documentation, see [docs/architecture/overview.md](docs/architecture/overview.md) and [docs/en/setup-guide.md](docs/en/setup-guide.md).
