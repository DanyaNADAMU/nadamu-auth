package mu.nada.nadamuauth.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import mu.nada.nadamuauth.config.PluginConfig;
import mu.nada.nadamuauth.security.SessionManager;
import mu.nada.nadamuauth.util.MessageService;

import java.util.Map;

public class PremiumCommand implements SimpleCommand {

    private final SessionManager sessionManager;
    private final PluginConfig pluginConfig;
    private final MessageService messageService;

    public PremiumCommand(SessionManager sessionManager,
                          PluginConfig pluginConfig,
                          MessageService messageService) {
        this.sessionManager = sessionManager;
        this.pluginConfig = pluginConfig;
        this.messageService = messageService;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(messageService.parse("<red>Эта команда доступна только игрокам!</red>"));
            return;
        }

        if (!pluginConfig.premium().enabled()) {
            messageService.sendMessage(player, "<red>Поддержка лицензий Mojang временно отключена администратором.</red>");
            return;
        }

        if (player.isOnlineMode()) {
            messageService.sendMessage(player, "<green>Вы уже успешно играете через подтверждённую лицензию Mojang!</green>");
            return;
        }

        String[] args = invocation.arguments();
        int timeout = pluginConfig.premium().verificationTimeoutMinutes();
        int attempts = pluginConfig.premium().maxFailedAttempts();

        if (args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            messageService.sendMessage(player, messageService.config().premiumPrompt(),
                    Map.of("minutes", String.valueOf(timeout), "attempts", String.valueOf(attempts)));
            return;
        }

        // Player executed /premium confirm
        sessionManager.startPremiumVerification(player.getUsername(), timeout);
        messageService.sendMessage(player, "<green>Заявка создана! Пожалуйста, перезайдите на сервер с лицензионного лаунчера в течение "
                + timeout + " минут (у вас " + attempts + " попытки).</green>");
    }
}
