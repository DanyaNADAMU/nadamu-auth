package mu.nada.nadamuauth.service;

import com.velocitypowered.api.proxy.Player;
import mu.nada.nadamuauth.config.PluginConfig;
import mu.nada.nadamuauth.model.AuthState;
import mu.nada.nadamuauth.model.UserAccount;
import mu.nada.nadamuauth.security.PasswordService;
import mu.nada.nadamuauth.security.RateLimiter;
import mu.nada.nadamuauth.security.SessionManager;
import mu.nada.nadamuauth.storage.UserRepository;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class LocalAuthService implements AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final RateLimiter rateLimiter;
    private final SessionManager sessionManager;
    private final PluginConfig.SecuritySettings securitySettings;
    private final Logger logger;

    public LocalAuthService(UserRepository userRepository,
                            PasswordService passwordService,
                            RateLimiter rateLimiter,
                            SessionManager sessionManager,
                            PluginConfig.SecuritySettings securitySettings,
                            Logger logger) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.rateLimiter = rateLimiter;
        this.sessionManager = sessionManager;
        this.securitySettings = securitySettings;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<AuthResult> authenticate(Player player, String password) {
        String ip = extractIp(player);
        String username = player.getUsername();

        if (rateLimiter.isLockedOut(ip, username)) {
            return CompletableFuture.completedFuture(AuthResult.RATE_LIMITED);
        }

        return userRepository.findByUsername(username).thenApply(optAccount -> {
            if (optAccount.isEmpty()) {
                return AuthResult.ACCOUNT_NOT_FOUND;
            }

            UserAccount account = optAccount.get();
            boolean matches = passwordService.verify(password, account.getPasswordHash());

            if (matches) {
                rateLimiter.reset(ip, username);
                sessionManager.setAuthState(player.getUniqueId(), AuthState.AUTHENTICATED);
                sessionManager.saveSession(player.getUniqueId(), ip);
                userRepository.updateLastLogin(player.getUniqueId(), ip);
                return AuthResult.SUCCESS;
            } else {
                rateLimiter.recordFailedAttempt(ip, username);
                return AuthResult.INVALID_CREDENTIALS;
            }
        }).exceptionally(ex -> {
            logger.error("Error during authentication for user {}", username, ex);
            return AuthResult.ERROR;
        });
    }

    @Override
    public CompletableFuture<RegistrationResult> register(Player player, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return CompletableFuture.completedFuture(RegistrationResult.PASSWORDS_DO_NOT_MATCH);
        }

        PasswordService.ValidationResult validation = passwordService.validate(
                password,
                securitySettings.minPasswordLength(),
                securitySettings.maxPasswordLength()
        );

        if (validation == PasswordService.ValidationResult.TOO_SHORT) {
            return CompletableFuture.completedFuture(RegistrationResult.PASSWORD_TOO_SHORT);
        } else if (validation == PasswordService.ValidationResult.TOO_LONG) {
            return CompletableFuture.completedFuture(RegistrationResult.PASSWORD_TOO_LONG);
        }

        String username = player.getUsername();
        String ip = extractIp(player);

        return userRepository.findByUsername(username).thenCompose(optAccount -> {
            if (optAccount.isPresent()) {
                return CompletableFuture.completedFuture(RegistrationResult.ALREADY_REGISTERED);
            }

            String hash = passwordService.hash(password);
            Instant now = Instant.now();
            UserAccount newAccount = new UserAccount(
                    player.getUniqueId(),
                    username,
                    hash,
                    ip,
                    now,
                    now,
                    false
            );

            return userRepository.create(newAccount).thenApply(acc -> {
                sessionManager.setAuthState(player.getUniqueId(), AuthState.AUTHENTICATED);
                sessionManager.saveSession(player.getUniqueId(), ip);
                return RegistrationResult.SUCCESS;
            });
        }).exceptionally(ex -> {
            logger.error("Error during registration for user {}", username, ex);
            return RegistrationResult.ERROR;
        });
    }

    @Override
    public CompletableFuture<Boolean> changePassword(Player player, String oldPassword, String newPassword) {
        return userRepository.findByUuid(player.getUniqueId()).thenCompose(optAccount -> {
            if (optAccount.isEmpty()) {
                return CompletableFuture.completedFuture(false);
            }

            UserAccount account = optAccount.get();
            if (!passwordService.verify(oldPassword, account.getPasswordHash())) {
                return CompletableFuture.completedFuture(false);
            }

            String newHash = passwordService.hash(newPassword);
            return userRepository.updatePassword(player.getUniqueId(), newHash).thenApply(v -> true);
        }).exceptionally(ex -> {
            logger.error("Error changing password for user {}", player.getUsername(), ex);
            return false;
        });
    }

    private String extractIp(Player player) {
        InetSocketAddress remoteAddress = player.getRemoteAddress();
        if (remoteAddress == null || remoteAddress.getAddress() == null) {
            return "127.0.0.1";
        }
        return remoteAddress.getAddress().getHostAddress();
    }
}
