# RFC / Idea: Authentik Integration

## Summary
Integrate NadamuAuth with an external **Authentik** Identity Provider (IdP) to allow web-based single sign-on (SSO), 2FA, Discord/Google social login, and Passkeys.

## Proposed Approaches

### Approach 1: OAuth 2.0 Device Authorization Grant (RFC 8628) — *Recommended*
- **User Experience**:
  1. Player types `/auth web` in Minecraft chat.
  2. Velocity calls Authentik's Device Authorization endpoint (`/application/o/device/code/`).
  3. Player receives a clickable link: `https://auth.example.com/device?user_code=ABCD-1234`.
  4. Player authenticates in their browser with full 2FA / Passkey support.
  5. Velocity polls Authentik in the background; once approved, player is authenticated on proxy and sent to Lobby.
- **Pros**: Zero password exposure in Minecraft chat; full 2FA/WebAuthn support; modern console-like UX.
- **Cons**: Requires browser interaction during first-time or expired logins.

### Approach 2: Direct REST API / LDAP Verification
- **User Experience**:
  1. Player executes `/login <authentik_password>`.
  2. Velocity queries Authentik's Core API to validate credentials.
- **Pros**: Seamless in-game experience.
- **Cons**: Exposes web passwords in game chat; does not support hardware 2FA tokens.

## Architecture Enabler
The `AuthService` interface created in Phase 1 allows introducing an `AuthentikAuthService` implementing `AuthService` without modifying listeners or routing logic.
