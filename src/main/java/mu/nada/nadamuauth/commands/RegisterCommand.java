package mu.nada.nadamuauth.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import mu.nada.nadamuauth.config.MessagesConfig;
import mu.nada.nadamuauth.config.PluginConfig;
import mu.nada.nadamuauth.security.SessionManager;
import mu.nada.nadamuauth.service.AuthService;
import mu.nada.nadamuauth.service.RegistrationResult;
import mu.nada.nadamuauth.service.RoutingService;
import mu.nada.nadamuauth.util.MessageService;

import java.util.Map;

public class RegisterCommand implements SimpleCommand {

    private final AuthService authService;
    private final SessionManager sessionManager;
    private final RoutingService routingService;
    private final PluginConfig pluginConfig;
    private final MessageService messageService;

    public RegisterCommand(AuthService authService,
                           SessionManager sessionManager,
                           RoutingService routingService,
                           PluginConfig pluginConfig,
                           MessageService messageService) {
        this.authService = authService;
        this.sessionManager = sessionManager;
        this.routingService = routingService;
        this.pluginConfig = pluginConfig;
        this.messageService = messageService;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(messageService.getComponent(invocation.source(), MessagesConfig::onlyPlayers));
            return;
        }

        if (sessionManager.isAuthenticated(player.getUniqueId())) {
            messageService.sendMessage(player, MessagesConfig::alreadyLoggedIn);
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 2) {
            messageService.sendMessage(player, MessagesConfig::registerUsage);
            return;
        }

        String password = args[0];
        String confirmPassword = args[1];

        authService.register(player, password, confirmPassword).thenAccept(result -> {
            if (result == RegistrationResult.SUCCESS) {
                messageService.sendMessage(player, MessagesConfig::registerSuccess);
                routingService.routeAfterAuth(player);
            } else if (result == RegistrationResult.ALREADY_REGISTERED) {
                messageService.sendMessage(player, MessagesConfig::alreadyRegistered);
            } else if (result == RegistrationResult.PASSWORDS_DO_NOT_MATCH) {
                messageService.sendMessage(player, MessagesConfig::passwordsDoNotMatch);
            } else if (result == RegistrationResult.PASSWORD_TOO_SHORT) {
                messageService.sendMessage(player, MessagesConfig::passwordTooShort,
                        Map.of("min", String.valueOf(pluginConfig.security().minPasswordLength())));
            } else if (result == RegistrationResult.PASSWORD_TOO_LONG) {
                messageService.sendMessage(player, MessagesConfig::passwordTooLong,
                        Map.of("max", String.valueOf(pluginConfig.security().maxPasswordLength())));
            } else {
                messageService.sendMessage(player, MessagesConfig::internalError);
            }
        });
    }
}
