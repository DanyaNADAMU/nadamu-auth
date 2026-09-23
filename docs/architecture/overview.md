# System Architecture Overview

`NadamuAuth` is a high-performance proxy-level authentication plugin designed for Velocity 4.0+.

## High-Level Topology

```mermaid
graph TD
    Client[Minecraft Client] -->|Connection| Velocity[Velocity Proxy + NadamuAuth]
    
    subgraph Proxy Layer
        Velocity --> SessionMgr[SessionManager]
        Velocity --> RateLimiter[RateLimiter]
        Velocity --> RoutingSvc[RoutingService]
        Velocity --> LangMgr[LanguageManager]
        Velocity --> MsgSvc[MessageService]
        Velocity --> AuthService[AuthService Interface]
        AuthService --> LocalAuth[LocalAuthService]
        LocalAuth --> UserRepo[UserRepository]
        UserRepo --> DB[(H2 Database / HikariCP)]
    end

    subgraph Backend Servers
        Velocity -->|PENDING_LOGIN| NanoLimbo[NanoLimbo Server]
        Velocity -->|AUTHENTICATED or GUEST| TargetServer[Target Server / Lobby]
    end
```

## Connection Routing & State Machine

```mermaid
stateDiagram-v2
    [*] --> Connect: Player Joins
    Connect --> OnlineModeCheck: PreLoginEvent

    state OnlineModeCheck {
        [*] --> CheckPending: Has /premium pending?
        CheckPending --> TryOnline: Yes (< 2 attempts) -> forceOnlineMode
        CheckPending --> AbortPending: Yes (>= 2 attempts) -> forceOfflineMode
        CheckPending --> CheckDB: No -> check is_premium in DB
        CheckDB --> ForceOnline: is_premium == true -> forceOnlineMode
        CheckDB --> ForceOffline: is_premium == false -> forceOfflineMode
    }

    OnlineModeCheck --> ChooseInitialServer: PlayerChooseInitialServerEvent (PostOrder.LATE)

    state ChooseInitialServer {
        [*] --> CaptureTarget: Capture forced-hosts destination
        CaptureTarget --> CheckAuth: isOnlineMode OR Valid IP Session?
        CheckAuth --> RouteTarget: Yes -> AUTHENTICATED -> Target / Lobby
        CheckAuth --> CheckRegistered: No -> Registered in DB?
        CheckRegistered --> CheckDbSession: Yes -> Valid DB IP Session?
        CheckDbSession --> RouteTarget: Yes -> AUTHENTICATED -> Target / Lobby
        CheckDbSession --> RouteLimbo: No -> PENDING_LOGIN -> NanoLimbo
        CheckRegistered --> RouteGuest: No -> GUEST -> Target / Lobby
    }

    RouteLimbo --> WaitLogin: /login <password>
    WaitLogin --> RouteTarget: Success -> Target / Lobby
```

## Component Breakdown

1. **`NadamuAuthPlugin`**: Central lifecycle coordinator and dependency injector for proxy events and commands.
2. **`RoutingService`**: Manages player destination resolution, preserving `forced-hosts` (e.g. `pvp.example.com`) across authentication while enforcing safe fallback to `lobbyServer`.
3. **`SessionManager`**: In-memory state tracking (`GUEST`, `PENDING_LOGIN`, `AUTHENTICATED`), login timeout cancellation, and pending `/premium` request tracking.
4. **`RateLimiter`**: Sliding-window attempt tracking per IP and username to mitigate brute-force password attacks.
5. **`LanguageManager` & `MessageService`**: Dynamic multi-language dictionary loading (`languages/*.yml`), client locale auto-detection, and Adventure MiniMessage rendering with interactive components.
6. **`LocalAuthService`**: Implementation of `AuthService` handling password verification, registration, and BCrypt hashing.
7. **`DatabaseManager` & `UserRepository`**: Asynchronous persistence layer backed by embedded pure Java H2 and HikariCP connection pooling, providing persistent IP session lookup and guest-to-premium account creation.
8. **`ConnectionListener` & `RestrictionListener`**: Velocity event interceptors enforcing strict NanoLimbo isolation (command whitelist `/login`, `/l`, `/register`, `/r`, `/reg` and chat suppression), timeout management, and seamless post-auth routing.
