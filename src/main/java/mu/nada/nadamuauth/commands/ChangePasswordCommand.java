package mu.nada.nadamuauth.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
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
            invocation.source().sendMessage(messageService.parse("<red>Эта команда доступна только игрокам!</red>"));
            return;
        }

        if (!sessionManager.isAuthenticated(player.getUniqueId())) {
            messageService.sendMessage(player, messageService.config().loginRequired());
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 2) {
            messageService.sendMessage(player, "<yellow>Использование: <aqua>/changepassword <старыйПароль> <новыйПароль></aqua></yellow>");
            return;
        }

        String oldPassword = args[0];
        String newPassword = args[1];

        authService.changePassword(player, oldPassword, newPassword).thenAccept(success -> {
            if (success) {
                messageService.sendMessage(player, messageService.config().passwordChanged());
            } else {
                messageService.sendMessage(player, messageService.config().wrongOldPassword());
            }
        });
    }
}
