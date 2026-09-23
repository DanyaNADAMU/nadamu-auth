package mu.nada.nadamuauth.listeners;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import mu.nada.nadamuauth.NadamuAuthPlugin;
import mu.nada.nadamuauth.config.MessagesConfig;
import mu.nada.nadamuauth.config.PluginConfig;
import mu.nada.nadamuauth.model.AuthState;
import mu.nada.nadamuauth.model.UserAccount;
import mu.nada.nadamuauth.security.SessionManager;
import mu.nada.nadamuauth.service.RoutingService;
import mu.nada.nadamuauth.storage.UserRepository;
import mu.nada.nadamuauth.util.MessageService;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class ConnectionListener {

    private final NadamuAuthPlugin plugin;
    private final ProxyServer server;
    private final PluginConfig pluginConfig;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final RoutingService routingService;
    private final MessageService messageService;

    public ConnectionListener(NadamuAuthPlugin plugin,
                              ProxyServer server,
                              PluginConfig pluginConfig,
                              UserRepository userRepository,
                              SessionManager sessionManager,
                              RoutingService routingService,
                              MessageService messageService) {
        this.plugin = plugin;
        this.server = server;
        this.pluginConfig = pluginConfig;
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
        this.routingService = routingService;
        this.messageService = messageService;
    }

    @Subscribe
    public EventTask onPreLogin(PreLoginEvent event) {
        String username = event.getUsername();

        // 1. Check pending /premium verification
        if (sessionManager.hasPendingPremium(username)) {
            int attempts = sessionManager.getPremiumAttempts(username);
            if (attempts >= pluginConfig.premium().maxFailedAttempts()) {
                sessionManager.clearPendingPremium(username);
                sessionManager.markFailedNotice(username);
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
                return EventTask.async(() -> {});
            } else {
                sessionManager.incrementPremiumAttempt(username);
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
                return EventTask.async(() -> {});
            }
        }

        // 2. Check if player has already confirmed Mojang license in database
        return EventTask.resumeWhenComplete(userRepository.findByUsername(username).thenAccept(optUser -> {
            if (optUser.isPresent() && optUser.get().isPremium()) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
            } else {
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
            }
        }));
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        String username = player.getUsername();

        // Notify about failed /premium verification if any
        if (sessionManager.hasFailedNotice(username)) {
            sessionManager.clearFailedNotice(username);
            messageService.sendMessage(player, MessagesConfig::premiumFailed);
        }

        // If player connected with licensed client and had pending /premium verification
        if (player.isOnlineMode() && sessionManager.hasPendingPremium(username)) {
            sessionManager.clearPendingPremium(username);
            String ip = extractIp(player);
            userRepository.findByUsername(username).thenAccept(optUser -> {
                if (optUser.isPresent()) {
                    userRepository.setPremium(optUser.get().getUuid(), true);
                    userRepository.updateLastLogin(optUser.get().getUuid(), ip);
                } else {
                    // Guest confirmed premium directly: create permanent premium account
                    UserAccount newAccount = new UserAccount(
                            player.getUniqueId(),
                            player.getUsername(),
                            "",
                            ip,
                            Instant.now(),
                            Instant.now(),
                            true
                    );
                    userRepository.create(newAccount);
                }
            });
            sessionManager.setAuthState(player.getUniqueId(), AuthState.AUTHENTICATED);
            messageService.sendMessage(player, MessagesConfig::premiumSuccess);
        } else if (player.isOnlineMode()) {
            // Confirmed Mojang player: update last login IP & timestamp
            String ip = extractIp(player);
            userRepository.findByUsername(username).thenAccept(optUser -> {
                optUser.ifPresent(u -> userRepository.updateLastLogin(u.getUuid(), ip));
            });
        }
    }

    @Subscribe(order = PostOrder.LATE)
    public EventTask onChooseInitialServer(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String ip = extractIp(player);
        String authServer = pluginConfig.servers().authServer();
        String lobbyServer = pluginConfig.servers().lobbyServer();

        // 1. Capture intended server from forced-hosts or try if not the auth server
        event.getInitialServer().ifPresent(srv -> {
            String name = srv.getServerInfo().getName();
            if (!name.equalsIgnoreCase(authServer)) {
                sessionManager.setTargetServer(uuid, name);
            }
        });

        String destination = routingService.getInitialDestination(uuid);

        // If player is already authenticated (e.g. via Mojang online mode)
        if (player.isOnlineMode() || sessionManager.isAuthenticated(uuid)) {
            sessionManager.setAuthState(uuid, AuthState.AUTHENTICATED);
            server.getServer(destination).ifPresent(event::setInitialServer);
            return EventTask.async(() -> {});
        }

        // If valid in-memory session exists for current IP
        if (sessionManager.hasValidSession(uuid, ip)) {
            sessionManager.setAuthState(uuid, AuthState.AUTHENTICATED);
            server.getServer(destination).ifPresent(event::setInitialServer);
            return EventTask.async(() -> {});
        }

        // Query database
        return EventTask.resumeWhenComplete(userRepository.findByUsername(player.getUsername()).thenAccept(optUser -> {
            if (optUser.isPresent()) {
                UserAccount user = optUser.get();

                // Check persistent IP session for password-based players
                if (!user.isPremium() && isSessionValid(user, ip)) {
                    sessionManager.setAuthState(uuid, AuthState.AUTHENTICATED);
                    sessionManager.saveSession(uuid, ip);
                    userRepository.updateLastLogin(user.getUuid(), ip);
                    server.getServer(destination).ifPresent(event::setInitialServer);
                    return;
                }

                // Registered player -> route to NanoLimbo for /login
                sessionManager.setAuthState(uuid, AuthState.PENDING_LOGIN);
                server.getServer(authServer).ifPresent(event::setInitialServer);
                messageService.sendMessage(player, MessagesConfig::loginRequired);
                scheduleLoginTimeout(player);
            } else {
                // Unregistered player -> Variant A (Guest mode, route to destination)
                sessionManager.setAuthState(uuid, AuthState.GUEST);
                server.getServer(destination).ifPresent(event::setInitialServer);
                messageService.sendMessage(player, MessagesConfig::guestReminder);
            }
        }));
    }

    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent event) {
        Player player = event.getPlayer();
        // If an authenticated player is kicked or backend server restarts (failover to limbo),
        // ensure their session remains AUTHENTICATED so they aren't trapped or kicked by timeout
        if (sessionManager.isAuthenticated(player.getUniqueId())) {
            // Keep authenticated status
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        sessionManager.removePlayer(event.getPlayer().getUniqueId());
    }

    private void scheduleLoginTimeout(Player player) {
        int timeout = pluginConfig.security().loginTimeoutSeconds();
        ScheduledTask task = server.getScheduler().buildTask(plugin, () -> {
            if (player.isActive() && sessionManager.getAuthState(player.getUniqueId()) == AuthState.PENDING_LOGIN) {
                player.disconnect(messageService.getComponent(player, MessagesConfig::timeoutKick));
            }
        }).delay(Duration.ofSeconds(timeout)).schedule();
        sessionManager.setTimeoutTask(player.getUniqueId(), task);
    }

    private String extractIp(Player player) {
        InetSocketAddress remoteAddress = player.getRemoteAddress();
        if (remoteAddress == null || remoteAddress.getAddress() == null) {
            return "127.0.0.1";
        }
        return remoteAddress.getAddress().getHostAddress();
    }

    private boolean isSessionValid(UserAccount user, String currentIp) {
        int timeoutMinutes = pluginConfig.security().sessionTimeoutMinutes();
        if (timeoutMinutes <= 0) {
            return false;
        }
        if (user.getLastIp() == null || !user.getLastIp().equals(currentIp)) {
            return false;
        }
        if (user.getLastLoginAt() == null) {
            return false;
        }
        Instant expiresAt = user.getLastLoginAt().plus(Duration.ofMinutes(timeoutMinutes));
        return Instant.now().isBefore(expiresAt);
    }
}
