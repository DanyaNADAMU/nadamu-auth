package mu.nada.nadamuauth.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import mu.nada.nadamuauth.model.AuthState;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Map<UUID, AuthState> activeStates = new ConcurrentHashMap<>();
    private final Map<UUID, String> targetServers = new ConcurrentHashMap<>();
    private final Cache<UUID, String> ipSessions;
    private final Map<String, PremiumVerification> pendingPremium = new ConcurrentHashMap<>();
    private final Set<String> failedPremiumNotices = ConcurrentHashMap.newKeySet();

    public SessionManager(int sessionTimeoutMinutes) {
        this.ipSessions = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(sessionTimeoutMinutes))
                .build();
    }

    public void setAuthState(UUID uuid, AuthState state) {
        activeStates.put(uuid, state);
    }

    public AuthState getAuthState(UUID uuid) {
        return activeStates.getOrDefault(uuid, AuthState.GUEST);
    }

    public boolean isAuthenticated(UUID uuid) {
        return activeStates.get(uuid) == AuthState.AUTHENTICATED;
    }

    public void setTargetServer(UUID uuid, String serverName) {
        if (serverName != null) {
            targetServers.put(uuid, serverName);
        }
    }

    public String getTargetServer(UUID uuid) {
        return targetServers.get(uuid);
    }

    public void removePlayer(UUID uuid) {
        activeStates.remove(uuid);
        targetServers.remove(uuid);
    }

    public void saveSession(UUID uuid, String ip) {
        ipSessions.put(uuid, ip);
    }

    public boolean hasValidSession(UUID uuid, String currentIp) {
        String savedIp = ipSessions.getIfPresent(uuid);
        return savedIp != null && savedIp.equals(currentIp);
    }

    public void invalidateSession(UUID uuid) {
        ipSessions.invalidate(uuid);
    }

    // --- Premium Verification (tracked by username) ---

    public void startPremiumVerification(String username, int timeoutMinutes) {
        pendingPremium.put(username.toLowerCase(), new PremiumVerification(Instant.now().plus(Duration.ofMinutes(timeoutMinutes))));
    }

    public boolean hasPendingPremium(String username) {
        PremiumVerification pv = pendingPremium.get(username.toLowerCase());
        if (pv == null) {
            return false;
        }
        if (Instant.now().isAfter(pv.expiresAt)) {
            pendingPremium.remove(username.toLowerCase());
            return false;
        }
        return true;
    }

    public int getPremiumAttempts(String username) {
        PremiumVerification pv = pendingPremium.get(username.toLowerCase());
        return pv != null ? pv.attempts : 0;
    }

    public void incrementPremiumAttempt(String username) {
        PremiumVerification pv = pendingPremium.get(username.toLowerCase());
        if (pv != null) {
            pv.attempts++;
        }
    }

    public void clearPendingPremium(String username) {
        pendingPremium.remove(username.toLowerCase());
    }

    public void markFailedNotice(String username) {
        failedPremiumNotices.add(username.toLowerCase());
    }

    public boolean hasFailedNotice(String username) {
        return failedPremiumNotices.contains(username.toLowerCase());
    }

    public void clearFailedNotice(String username) {
        failedPremiumNotices.remove(username.toLowerCase());
    }

    private static class PremiumVerification {
        private final Instant expiresAt;
        private int attempts = 0;

        public PremiumVerification(Instant expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}
