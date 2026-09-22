package mu.nada.nadamuauth.storage;

import mu.nada.nadamuauth.model.UserAccount;
import org.slf4j.Logger;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public class UserRepository {

    private final DatabaseManager databaseManager;
    private final Logger logger;
    private final Executor executor;

    public UserRepository(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
        this.executor = ForkJoinPool.commonPool();
    }

    public CompletableFuture<Optional<UserAccount>> findByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT uuid, username, password_hash, last_ip, registered_at, last_login_at, is_premium FROM users WHERE username_lower = ?";
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, username.toLowerCase());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                logger.error("Failed to query user by username: {}", username, e);
            }
            return Optional.empty();
        }, executor);
    }

    public CompletableFuture<Optional<UserAccount>> findByUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT uuid, username, password_hash, last_ip, registered_at, last_login_at, is_premium FROM users WHERE uuid = ?";
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                logger.error("Failed to query user by UUID: {}", uuid, e);
            }
            return Optional.empty();
        }, executor);
    }

    public CompletableFuture<UserAccount> create(UserAccount account) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                    INSERT INTO users (uuid, username, username_lower, password_hash, last_ip, registered_at, last_login_at, is_premium)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, account.getUuid().toString());
                ps.setString(2, account.getUsername());
                ps.setString(3, account.getUsername().toLowerCase());
                ps.setString(4, account.getPasswordHash());
                ps.setString(5, account.getLastIp());
                ps.setTimestamp(6, Timestamp.from(account.getRegisteredAt()));
                ps.setTimestamp(7, Timestamp.from(account.getLastLoginAt()));
                ps.setBoolean(8, account.isPremium());
                ps.executeUpdate();
                return account;
            } catch (SQLException e) {
                logger.error("Failed to create user account: {}", account.getUsername(), e);
                throw new RuntimeException("User creation failed", e);
            }
        }, executor);
    }

    public CompletableFuture<Void> updatePassword(UUID uuid, String newPasswordHash) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE users SET password_hash = ? WHERE uuid = ?";
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, newPasswordHash);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.error("Failed to update password for UUID: {}", uuid, e);
            }
        }, executor);
    }

    public CompletableFuture<Void> updateLastLogin(UUID uuid, String ip) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE users SET last_ip = ?, last_login_at = ? WHERE uuid = ?";
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, ip);
                ps.setTimestamp(2, Timestamp.from(Instant.now()));
                ps.setString(3, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.error("Failed to update last login for UUID: {}", uuid, e);
            }
        }, executor);
    }

    public CompletableFuture<Void> setPremium(UUID uuid, boolean isPremium) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE users SET is_premium = ? WHERE uuid = ?";
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setBoolean(1, isPremium);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.error("Failed to update premium status for UUID: {}", uuid, e);
            }
        }, executor);
    }

    public CompletableFuture<Void> delete(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM users WHERE uuid = ?";
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.error("Failed to delete user with UUID: {}", uuid, e);
            }
        }, executor);
    }

    private UserAccount mapResultSet(ResultSet rs) throws SQLException {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String lastIp = rs.getString("last_ip");
        Instant registeredAt = rs.getTimestamp("registered_at").toInstant();
        Instant lastLoginAt = rs.getTimestamp("last_login_at").toInstant();
        boolean isPremium = rs.getBoolean("is_premium");

        return new UserAccount(uuid, username, passwordHash, lastIp, registeredAt, lastLoginAt, isPremium);
    }
}
