package mu.nada.nadamuauth.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import mu.nada.nadamuauth.config.MessagesConfig;
import mu.nada.nadamuauth.security.SessionManager;
import mu.nada.nadamuauth.service.AuthService;
import mu.nada.nadamuauth.util.MessageService;

public class ChangePasswordCommand implements SimpleCommand {

    private final AuthService authService;
    private final SessionManager sessionManager;
    private final MessageService messageService;

    public ChangePasswordCommand(AuthService authService,
                                 SessionManager sessionManager,
                                 MessageService messageService) {
        this.authService = authService;
        this.sessionManager = sessionManager;
        this.messageService = messageService;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(messageService.getComponent(invocation.source(), MessagesConfig::onlyPlayers));
            return;
        }

        if (!sessionManager.isAuthenticated(player.getUniqueId())) {
            messageService.sendMessage(player, MessagesConfig::loginRequired);
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 2) {
            messageService.sendMessage(player, MessagesConfig::changePasswordUsage);
            return;
        }

        String oldPassword = args[0];
        String newPassword = args[1];

        authService.changePassword(player, oldPassword, newPassword).thenAccept(success -> {
            if (success) {
                messageService.sendMessage(player, MessagesConfig::passwordChanged);
            } else {
                messageService.sendMessage(player, MessagesConfig::wrongOldPassword);
            }
        });
    }
}
