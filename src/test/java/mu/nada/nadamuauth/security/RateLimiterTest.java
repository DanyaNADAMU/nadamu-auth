package mu.nada.nadamuauth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter(3, 10);
    }

    @Test
    void testFailedAttemptsAndLockout() {
        String ip = "192.168.1.100";
        String username = "TestUser";

        assertFalse(rateLimiter.isLockedOut(ip, username));
        assertEquals(3, rateLimiter.getRemainingAttempts(ip, username));

        rateLimiter.recordFailedAttempt(ip, username);
        assertEquals(2, rateLimiter.getRemainingAttempts(ip, username));
        assertFalse(rateLimiter.isLockedOut(ip, username));

        rateLimiter.recordFailedAttempt(ip, username);
        assertEquals(1, rateLimiter.getRemainingAttempts(ip, username));

        rateLimiter.recordFailedAttempt(ip, username);
        assertEquals(0, rateLimiter.getRemainingAttempts(ip, username));
        assertTrue(rateLimiter.isLockedOut(ip, username));

        rateLimiter.reset(ip, username);
        assertFalse(rateLimiter.isLockedOut(ip, username));
        assertEquals(3, rateLimiter.getRemainingAttempts(ip, username));
    }
}
