package mu.nada.nadamuauth.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import mu.nada.nadamuauth.config.PluginConfig;
import mu.nada.nadamuauth.security.RateLimiter;
import mu.nada.nadamuauth.security.SessionManager;
import mu.nada.nadamuauth.service.AuthResult;
import mu.nada.nadamuauth.service.AuthService;
import mu.nada.nadamuauth.service.RoutingService;
import mu.nada.nadamuauth.util.MessageService;

import java.net.InetSocketAddress;
import java.util.Map;

public class LoginCommand implements SimpleCommand {

    private final AuthService authService;
    private final SessionManager sessionManager;
    private final RateLimiter rateLimiter;
    private final RoutingService routingService;
    private final MessageService messageService;

    public LoginCommand(AuthService authService,
                        SessionManager sessionManager,
                        RateLimiter rateLimiter,
                        RoutingService routingService,
                        MessageService messageService) {
        this.authService = authService;
        this.sessionManager = sessionManager;
        this.rateLimiter = rateLimiter;
        this.routingService = routingService;
        this.messageService = messageService;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(messageService.parse("<red>Эта команда доступна только игрокам!</red>"));
            return;
        }

        if (sessionManager.isAuthenticated(player.getUniqueId())) {
            messageService.sendMessage(player, messageService.config().alreadyLoggedIn());
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 1) {
            messageService.sendMessage(player, messageService.config().loginRequired());
            return;
        }

        String password = args[0];
        String ip = extractIp(player);

        authService.authenticate(player, password).thenAccept(result -> {
            if (result == AuthResult.SUCCESS) {
                messageService.sendMessage(player, messageService.config().loginSuccess());
                routingService.routeAfterAuth(player);
            } else if (result == AuthResult.INVALID_CREDENTIALS) {
                int left = rateLimiter.getRemainingAttempts(ip, player.getUsername());
                messageService.sendMessage(player, messageService.config().wrongPassword(), Map.of("left", String.valueOf(left)));
            } else if (result == AuthResult.RATE_LIMITED) {
                messageService.sendMessage(player, messageService.config().lockoutMessage(),
                        Map.of("minutes", String.valueOf(rateLimiter.getLockoutMinutes())));
            } else if (result == AuthResult.ACCOUNT_NOT_FOUND) {
                messageService.sendMessage(player, messageService.config().guestReminder());
            } else {
                messageService.sendMessage(player, "<red>Произошла внутренняя ошибка при авторизации. Попробуйте позже.</red>");
            }
        });
    }

    private String extractIp(Player player) {
        InetSocketAddress address = player.getRemoteAddress();
        if (address == null || address.getAddress() == null) {
            return "127.0.0.1";
        }
        return address.getAddress().getHostAddress();
    }
}
