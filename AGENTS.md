# AGENTS.md

## Project Context
- **Project Name**: NadamuAuth
- **Target Platform**: Velocity Proxy (version 4.0.0+)
- **Runtime**: Java 25
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`)
- **Group**: `mu.nada`
- **Artifact**: `nadamu-auth`

## Key Architecture Principles
1. **Separation of Concerns**:
   - `service/` encapsulates authentication operations (`AuthService`, `LocalAuthService`).
   - `security/` handles hashing, rate limiting, and session cache (`PasswordService`, `RateLimiter`, `SessionManager`).
   - `storage/` manages database connections and async repository operations (`DatabaseManager`, `UserRepository`).
   - `listeners/` intercepts Velocity network and player events.
   - `commands/` provides CLI user and admin interfaces.
2. **Database**:
   - Embedded pure Java H2 database with HikariCP connection pooling.
   - All repository calls return `CompletableFuture` to avoid blocking Velocity event loops.
3. **Player Lifecycle & Authentication**:
   - **Guest Mode (Variant A)**: Unregistered players are routed directly to `lobby` and periodically reminded to register.
   - **Registered Mode**: Registered players are routed to `limbo` (NanoLimbo) and must `/login <password>`.
   - **Premium Verification**: `/premium confirm` creates a 5-minute / 2-attempt window where `forceOnlineMode()` is attempted on reconnect.
4. **Authentik Extensibility**:
   - The `AuthService` interface allows swapping or chaining `LocalAuthService` with an OAuth 2.0 Device Flow or REST-based Authentik provider in the future.

## Code Standards & Conventions
- All code, code comments, and technical documentation must be exclusively in English.
- Tests in `src/test/java/` serve as living specifications for security and session state machines.
