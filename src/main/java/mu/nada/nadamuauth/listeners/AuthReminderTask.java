package mu.nada.nadamuauth.listeners;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import mu.nada.nadamuauth.config.MessagesConfig;
import mu.nada.nadamuauth.model.AuthState;
import mu.nada.nadamuauth.security.SessionManager;
import mu.nada.nadamuauth.util.MessageService;

public class AuthReminderTask implements Runnable {

    private final ProxyServer server;
    private final SessionManager sessionManager;
    private final MessageService messageService;

    public AuthReminderTask(ProxyServer server, SessionManager sessionManager, MessageService messageService) {
        this.server = server;
        this.sessionManager = sessionManager;
        this.messageService = messageService;
    }

    @Override
    public void run() {
        for (Player player : server.getAllPlayers()) {
            AuthState state = sessionManager.getAuthState(player.getUniqueId());

            if (state == AuthState.GUEST) {
                messageService.sendMessage(player, MessagesConfig::guestReminder);
            } else if (state == AuthState.PENDING_LOGIN) {
                messageService.sendMessage(player, MessagesConfig::loginRequired);
            }
        }
    }
}
