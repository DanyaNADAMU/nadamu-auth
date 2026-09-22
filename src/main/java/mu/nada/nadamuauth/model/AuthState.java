package mu.nada.nadamuauth.model;

public enum AuthState {
    /**
     * Unregistered player, playing in guest mode on lobby.
     * Receives non-intrusive registration reminders.
     */
    GUEST,

    /**
     * Registered player, currently on auth server (NanoLimbo)
     * waiting to execute /login <password>.
     */
    PENDING_LOGIN,

    /**
     * Authenticated player (entered password, auto-logged in via IP session or Mojang license).
     * Has full access to the proxy server network.
     */
    AUTHENTICATED
}
