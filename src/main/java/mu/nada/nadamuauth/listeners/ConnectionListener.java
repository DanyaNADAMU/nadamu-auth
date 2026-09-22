package mu.nada.nadamuauth.listeners;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import mu.nada.nadamuauth.NadamuAuthPlugin;
import mu.nada.nadamuauth.config.PluginConfig;
import mu.nada.nadamuauth.model.AuthState;
import mu.nada.nadamuauth.security.SessionManager;
import mu.nada.nadamuauth.storage.UserRepository;
import mu.nada.nadamuauth.util.MessageService;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.UUID;

public class ConnectionListener {

    private final NadamuAuthPlugin plugin;
    private final ProxyServer server;
    private final PluginConfig pluginConfig;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final MessageService messageService;

    public ConnectionListener(NadamuAuthPlugin plugin,
                              ProxyServer server,
                              PluginConfig pluginConfig,
                              UserRepository userRepository,
                              SessionManager sessionManager,
                              MessageService messageService) {
        this.plugin = plugin;
        this.server = server;
        this.pluginConfig = pluginConfig;
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
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
            messageService.sendMessage(player, messageService.config().premiumFailed());
        }

        // If player connected with licensed client and had pending /premium verification
        if (player.isOnlineMode() && sessionManager.hasPendingPremium(username)) {
            sessionManager.clearPendingPremium(username);
            userRepository.findByUsername(username).thenAccept(optUser -> {
                if (optUser.isPresent()) {
                    userRepository.setPremium(optUser.get().getUuid(), true);
                }
            });
            sessionManager.setAuthState(player.getUniqueId(), AuthState.AUTHENTICATED);
            messageService.sendMessage(player, messageService.config().premiumSuccess());
        }
    }

    @Subscribe
    public EventTask onChooseInitialServer(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String ip = extractIp(player);

        // If player is already authenticated (e.g. via Mojang online mode)
        if (player.isOnlineMode() || sessionManager.isAuthenticated(uuid)) {
            sessionManager.setAuthState(uuid, AuthState.AUTHENTICATED);
            server.getServer(pluginConfig.servers().lobbyServer()).ifPresent(event::setInitialServer);
            return EventTask.async(() -> {});
        }

        // If valid session exists for current IP
        if (sessionManager.hasValidSession(uuid, ip)) {
            sessionManager.setAuthState(uuid, AuthState.AUTHENTICATED);
            server.getServer(pluginConfig.servers().lobbyServer()).ifPresent(event::setInitialServer);
            return EventTask.async(() -> {});
        }

        // Query database
        return EventTask.resumeWhenComplete(userRepository.findByUsername(player.getUsername()).thenAccept(optUser -> {
            if (optUser.isPresent()) {
                // Registered player -> route to NanoLimbo for /login
                sessionManager.setAuthState(uuid, AuthState.PENDING_LOGIN);
                server.getServer(pluginConfig.servers().authServer()).ifPresent(event::setInitialServer);
                messageService.sendMessage(player, messageService.config().loginRequired());
                scheduleLoginTimeout(player);
            } else {
                // Unregistered player -> Variant A (Guest mode, route directly to Lobby)
                sessionManager.setAuthState(uuid, AuthState.GUEST);
                server.getServer(pluginConfig.servers().lobbyServer()).ifPresent(event::setInitialServer);
                messageService.sendMessage(player, messageService.config().guestReminder());
            }
        }));
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        sessionManager.removePlayer(event.getPlayer().getUniqueId());
    }

    private void scheduleLoginTimeout(Player player) {
        int timeout = pluginConfig.security().loginTimeoutSeconds();
        server.getScheduler().buildTask(plugin, () -> {
            if (player.isActive() && sessionManager.getAuthState(player.getUniqueId()) == AuthState.PENDING_LOGIN) {
                player.disconnect(messageService.parse(messageService.config().timeoutKick()));
            }
        }).delay(Duration.ofSeconds(timeout)).schedule();
    }

    private String extractIp(Player player) {
        InetSocketAddress remoteAddress = player.getRemoteAddress();
        if (remoteAddress == null || remoteAddress.getAddress() == null) {
            return "127.0.0.1";
        }
        return remoteAddress.getAddress().getHostAddress();
    }
}
