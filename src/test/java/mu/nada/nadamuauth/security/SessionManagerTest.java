package mu.nada.nadamuauth.security;

import mu.nada.nadamuauth.model.AuthState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager(60);
    }

    @Test
    void testAuthStateManagement() {
        UUID uuid = UUID.randomUUID();

        assertEquals(AuthState.GUEST, sessionManager.getAuthState(uuid));

        sessionManager.setAuthState(uuid, AuthState.PENDING_LOGIN);
        assertEquals(AuthState.PENDING_LOGIN, sessionManager.getAuthState(uuid));
        assertFalse(sessionManager.isAuthenticated(uuid));

        sessionManager.setAuthState(uuid, AuthState.AUTHENTICATED);
        assertEquals(AuthState.AUTHENTICATED, sessionManager.getAuthState(uuid));
        assertTrue(sessionManager.isAuthenticated(uuid));

        sessionManager.removePlayer(uuid);
        assertEquals(AuthState.GUEST, sessionManager.getAuthState(uuid));
    }

    @Test
    void testIpSession() {
        UUID uuid = UUID.randomUUID();
        String ip = "127.0.0.1";

        assertFalse(sessionManager.hasValidSession(uuid, ip));

        sessionManager.saveSession(uuid, ip);
        assertTrue(sessionManager.hasValidSession(uuid, ip));
        assertFalse(sessionManager.hasValidSession(uuid, "192.168.1.1"));

        sessionManager.invalidateSession(uuid);
        assertFalse(sessionManager.hasValidSession(uuid, ip));
    }

    @Test
    void testPremiumVerification() {
        String username = "Player123";

        assertFalse(sessionManager.hasPendingPremium(username));

        sessionManager.startPremiumVerification(username, 5);
        assertTrue(sessionManager.hasPendingPremium(username));
        assertEquals(0, sessionManager.getPremiumAttempts(username));

        sessionManager.incrementPremiumAttempt(username);
        assertEquals(1, sessionManager.getPremiumAttempts(username));

        sessionManager.clearPendingPremium(username);
        assertFalse(sessionManager.hasPendingPremium(username));
    }
}
