# Phase 1: Core Authentication & Architecture

Execution plan and milestone tracking for the initial development phase of NadamuAuth.

## Checklist

### Foundation & Configuration
- [x] Create main plugin class `NadamuAuthPlugin` with Velocity DI.
- [x] Implement `PluginConfig` and `MessagesConfig` using Configurate (YAML).
- [x] Implement `MessageService` with Adventure MiniMessage support and placeholders.

### Data Layer & Storage
- [x] Configure HikariCP with H2 embedded database in `DatabaseManager`.
- [x] Implement `users` table schema and indexes.
- [x] Implement asynchronous `UserRepository` with `CompletableFuture`.

### Security & Sessions
- [x] Implement `PasswordService` with BCrypt (cost factor 12) and length validators.
- [x] Implement `RateLimiter` with Caffeine cache for IP and username brute-force prevention.
- [x] Implement `SessionManager` for active player auth states, IP session caching, and `/premium` tracking.
- [x] Implement `AuthService` interface and `LocalAuthService` implementation.

### Event Listeners & Restrictions
- [x] Implement `ConnectionListener` (`PreLoginEvent`, `LoginEvent`, `PlayerChooseInitialServerEvent`, `DisconnectEvent`).
- [x] Implement `RestrictionListener` (command filtering, chat suppression, server transfer blocking).
- [x] Implement `AuthReminderTask` for periodic guest reminders.

### Commands
- [x] `/login <password>` (alias `/l`)
- [x] `/register <password> <confirm>` (alias `/reg`)
- [x] `/changepassword <old> <new>`
- [x] `/premium [confirm]`
- [x] `/auth` (admin command: `reload`, `unregister`, `setpremium`, alias `/nauth`)

### Testing & Verification
- [x] Unit tests for `PasswordServiceTest`.
- [x] Unit tests for `RateLimiterTest`.
- [x] Unit tests for `SessionManagerTest`.
- [x] Successful `./gradlew test` and shadow JAR assembly.
