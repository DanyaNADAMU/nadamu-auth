# System Architecture Overview

`NadamuAuth` is a proxy-level authentication plugin designed for Velocity 4.0+.

## High-Level Topology

```mermaid
graph TD
    Client[Minecraft Client] -->|Connection| Velocity[Velocity Proxy + NadamuAuth]
    
    subgraph Proxy Layer
        Velocity --> SessionMgr[SessionManager]
        Velocity --> RateLimiter[RateLimiter]
        Velocity --> AuthService[AuthService Interface]
        AuthService --> LocalAuth[LocalAuthService]
        LocalAuth --> UserRepo[UserRepository]
        UserRepo --> DB[(H2 Database / HikariCP)]
    end

    subgraph Backend Servers
        Velocity -->|PENDING_LOGIN| NanoLimbo[NanoLimbo Server]
        Velocity -->|AUTHENTICATED or GUEST| Lobby[Lobby / Hub Server]
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

    OnlineModeCheck --> ChooseInitialServer: PlayerChooseInitialServerEvent

    state ChooseInitialServer {
        [*] --> CheckAuth: isOnlineMode OR Valid IP Session?
        CheckAuth --> RouteLobby: Yes -> AUTHENTICATED -> Lobby
        CheckAuth --> CheckRegistered: No -> Registered in DB?
        CheckRegistered --> RouteLimbo: Yes -> PENDING_LOGIN -> NanoLimbo
        CheckRegistered --> RouteGuest: No -> GUEST -> Lobby
    }

    RouteLimbo --> WaitLogin: /login <password>
    WaitLogin --> RouteLobby: Success -> Lobby
```

## Component Breakdown

1. **`NadamuAuthPlugin`**: Central lifecycle and dependency injector for proxy events and commands.
2. **`SessionManager`**: In-memory state tracking (`GUEST`, `PENDING_LOGIN`, `AUTHENTICATED`), IP-based session cache via Caffeine, and pending `/premium` request tracking.
3. **`RateLimiter`**: Sliding-window attempt tracking per IP and username to mitigate brute-force password guessing.
4. **`LocalAuthService`**: Concrete implementation of `AuthService` handling password verification and account creation.
5. **`DatabaseManager` & `UserRepository`**: Asynchronous persistence layer backed by H2 and HikariCP.
6. **`ConnectionListener` & `RestrictionListener`**: Velocity event interceptors enforcing chat, command, and server transfer restrictions.
