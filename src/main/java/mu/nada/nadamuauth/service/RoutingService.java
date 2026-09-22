package mu.nada.nadamuauth.service;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import mu.nada.nadamuauth.config.PluginConfig;
import mu.nada.nadamuauth.security.SessionManager;

import java.util.UUID;

public class RoutingService {

    private final ProxyServer server;
    private final PluginConfig pluginConfig;
    private final SessionManager sessionManager;

    public RoutingService(ProxyServer server, PluginConfig pluginConfig, SessionManager sessionManager) {
        this.server = server;
        this.pluginConfig = pluginConfig;
        this.sessionManager = sessionManager;
    }

    /**
     * Determines initial server destination.
     * Uses forced-host target if present and distinct from authServer, otherwise lobbyServer.
     */
    public String getInitialDestination(UUID uuid) {
        String authServer = pluginConfig.servers().authServer();
        String lobbyServer = pluginConfig.servers().lobbyServer();
        String forcedTarget = sessionManager.getTargetServer(uuid);

        if (forcedTarget != null && !forcedTarget.equalsIgnoreCase(authServer)) {
            return forcedTarget;
        }
        return lobbyServer;
    }

    /**
     * Routes an authenticated player after successful login or registration.
     * If the player connected via a forced host, attempts connecting to that target first.
     * If that attempt fails (or no forced host was used), always routes to lobbyServer.
     */
    public void routeAfterAuth(Player player) {
        String authServer = pluginConfig.servers().authServer();
        String lobbyServer = pluginConfig.servers().lobbyServer();
        String forcedTarget = sessionManager.getTargetServer(player.getUniqueId());

        if (forcedTarget != null && !forcedTarget.equalsIgnoreCase(authServer)) {
            // Player used a forced host. Try to connect to that target!
            server.getServer(forcedTarget).ifPresentOrElse(target -> {
                player.createConnectionRequest(target).connectWithIndication().thenAccept(success -> {
                    if (!success) {
                        // Connection to forced target failed, fall back to lobbyServer
                        fallbackToLobby(player, authServer, lobbyServer);
                    }
                });
            }, () -> fallbackToLobby(player, authServer, lobbyServer));
        } else {
            // No forced host, always go to lobbyServer
            fallbackToLobby(player, authServer, lobbyServer);
        }
    }

    private void fallbackToLobby(Player player, String authServer, String lobbyServer) {
        if (!authServer.equalsIgnoreCase(lobbyServer)) {
            server.getServer(lobbyServer).ifPresent(lobby -> {
                player.createConnectionRequest(lobby).connectWithIndication();
            });
        }
    }
}
