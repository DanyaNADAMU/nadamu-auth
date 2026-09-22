# ADR 0002: Safe Premium Verification State Machine

## Status
Accepted

## Context
When supporting both licensed (Mojang) and offline (cracked) clients on the same Minecraft proxy network, forcing online mode unconditionally for existing Mojang usernames prevents offline players with identical usernames from joining (the "shooting yourself in the foot" problem). Conversely, allowing password-only logins for licensed players compromises UX and convenience.

## Considered Options
1. **Automatic Mojang lookup during PreLogin**: Check Mojang API for nickname existence; if exists, force online mode.
   - *Flaw*: Any cracked player using a known licensed nickname is permanently locked out.
2. **Opt-in with permanent lock**: Player types `/premium`, permanently enabling online mode.
   - *Flaw*: If the player loses access to their Mojang account or made a typo, they are permanently locked out until an administrator intervenes.
3. **State-Machine Verification with Fallback (Chosen)**:
   - Player executes `/premium confirm`.
   - Verification state is active for 5 minutes with a maximum limit of 2 failed attempts.
   - On reconnect, proxy attempts `forceOnlineMode()`.
   - If successful, account is marked `is_premium = true`.
   - If failed twice or expired, status reverts to offline mode, and the player is notified.

## Decision
Implement the 5-minute / 2-attempt verification window in `SessionManager` and `ConnectionListener`.

## Consequences
- **Positive**: Zero risk of irreversible player lockout; self-service workflow; full security.
- **Negative**: Requires players to reconnect within 5 minutes after typing `/premium confirm`.
