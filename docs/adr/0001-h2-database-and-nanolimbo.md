# ADR 0001: Selection of H2 Database and NanoLimbo Integration

## Status
Accepted

## Context
Velocity authentication requires storing player credentials, registration dates, last IP addresses, and license flags. Additionally, unauthenticated players must be isolated from the backend game servers without exhausting server memory or CPU resources.

## Considered Options
1. **Database**:
   - **SQLite**: Widely used, but relies on C-based JNI libraries which can cause extraction and compatibility issues on customized Linux/Alpine containers, and locks the database file on concurrent writes.
   - **MySQL/PostgreSQL**: Scalable across multiple proxy nodes, but requires hosting and maintaining an external database daemon, which is unnecessary overhead for single-proxy networks.
   - **H2 (Chosen)**: 100% pure Java, runs in-process with zero native dependencies, supports MVStore engine for high concurrency, and seamlessly integrates with HikariCP.
2. **Auth Isolation**:
   - **Full Paper instance as Auth Lobby**: Heavy RAM/CPU footprint (~500MB+ RAM per instance) to simply hold players typing `/login`.
   - **NanoLimbo (Chosen)**: Minimalist headless protocol server using ~32-64MB RAM with instant player handoffs.

## Decision
- Use **H2** with HikariCP in MySQL compatibility mode (`MODE=MySQL;DATABASE_TO_UPPER=FALSE`) as the default storage engine.
- Route unauthenticated registered players to a **NanoLimbo** instance.

## Consequences
- **Positive**: Zero external dependencies; instant deployment; minimal RAM footprint; robust concurrency.
- **Negative**: H2 is local to the proxy node. If multi-proxy load balancing is adopted in the future, a migration path or remote database driver (MySQL/PostgreSQL) will be required.
