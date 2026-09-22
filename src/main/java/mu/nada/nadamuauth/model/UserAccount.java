package mu.nada.nadamuauth.model;

import java.time.Instant;
import java.util.UUID;

public class UserAccount {

    private final UUID uuid;
    private final String username;
    private String passwordHash;
    private String lastIp;
    private final Instant registeredAt;
    private Instant lastLoginAt;
    private boolean premium;

    public UserAccount(UUID uuid, String username, String passwordHash, String lastIp, Instant registeredAt, Instant lastLoginAt, boolean premium) {
        this.uuid = uuid;
        this.username = username;
        this.passwordHash = passwordHash;
        this.lastIp = lastIp;
        this.registeredAt = registeredAt;
        this.lastLoginAt = lastLoginAt;
        this.premium = premium;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }
}
