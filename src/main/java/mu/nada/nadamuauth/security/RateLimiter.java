package mu.nada.nadamuauth.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter {

    private final int maxAttempts;
    private final int lockoutMinutes;
    private final Cache<String, AtomicInteger> ipAttempts;
    private final Cache<String, AtomicInteger> userAttempts;

    public RateLimiter(int maxAttempts, int lockoutMinutes) {
        this.maxAttempts = maxAttempts;
        this.lockoutMinutes = lockoutMinutes;

        this.ipAttempts = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(lockoutMinutes))
                .build();

        this.userAttempts = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(lockoutMinutes))
                .build();
    }

    public boolean isLockedOut(String ip, String username) {
        AtomicInteger ipCount = ipAttempts.getIfPresent(ip);
        if (ipCount != null && ipCount.get() >= maxAttempts) {
            return true;
        }

        AtomicInteger userCount = userAttempts.getIfPresent(username.toLowerCase());
        return userCount != null && userCount.get() >= maxAttempts;
    }

    public int recordFailedAttempt(String ip, String username) {
        AtomicInteger ipCount = ipAttempts.get(ip, k -> new AtomicInteger(0));
        int currentIpAttempts = ipCount.incrementAndGet();

        AtomicInteger userCount = userAttempts.get(username.toLowerCase(), k -> new AtomicInteger(0));
        int currentUserAttempts = userCount.incrementAndGet();

        return Math.max(currentIpAttempts, currentUserAttempts);
    }

    public void reset(String ip, String username) {
        ipAttempts.invalidate(ip);
        userAttempts.invalidate(username.toLowerCase());
    }

    public int getRemainingAttempts(String ip, String username) {
        AtomicInteger ipCount = ipAttempts.getIfPresent(ip);
        AtomicInteger userCount = userAttempts.getIfPresent(username.toLowerCase());

        int current = Math.max(
                ipCount != null ? ipCount.get() : 0,
                userCount != null ? userCount.get() : 0
        );

        return Math.max(0, maxAttempts - current);
    }

    public int getLockoutMinutes() {
        return lockoutMinutes;
    }
}
