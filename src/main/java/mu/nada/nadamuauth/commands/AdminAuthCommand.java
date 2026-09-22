package mu.nada.nadamuauth.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import mu.nada.nadamuauth.config.ConfigManager;
import mu.nada.nadamuauth.security.SessionManager;
import mu.nada.nadamuauth.storage.UserRepository;
import mu.nada.nadamuauth.util.MessageService;
import org.spongepowered.configurate.ConfigurateException;

import java.util.List;

public class AdminAuthCommand implements SimpleCommand {

    private final ConfigManager configManager;
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final MessageService messageService;

    public AdminAuthCommand(ConfigManager configManager,
                            UserRepository userRepository,
                            SessionManager sessionManager,
                            MessageService messageService) {
        this.configManager = configManager;
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
        this.messageService = messageService;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!source.hasPermission("nadamuauth.admin")) {
            source.sendMessage(messageService.parse("<red>У вас нет прав на использование этой команды!</red>"));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length == 0) {
            source.sendMessage(messageService.parse("<yellow>Команды NadamuAuth:</yellow><br>"
                    + "<aqua>/auth reload</aqua> - Перезагрузить конфигурацию<br>"
                    + "<aqua>/auth unregister <ник></aqua> - Удалить аккаунт игрока<br>"
                    + "<aqua>/auth setpremium <ник> <true|false></aqua> - Установить статус лицензии"));
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> {
                try {
                    configManager.reload();
                    source.sendMessage(messageService.parse("<green>Конфигурация и сообщения успешно перезагружены!</green>"));
                } catch (ConfigurateException e) {
                    source.sendMessage(messageService.parse("<red>Ошибка при перезагрузке конфигурации: " + e.getMessage() + "</red>"));
                }
            }
            case "unregister" -> {
                if (args.length < 2) {
                    source.sendMessage(messageService.parse("<yellow>Использование: /auth unregister <ник></yellow>"));
                    return;
                }
                String targetName = args[1];
                userRepository.findByUsername(targetName).thenAccept(optUser -> {
                    if (optUser.isEmpty()) {
                        source.sendMessage(messageService.parse("<red>Пользователь " + targetName + " не найден в базе данных!</red>"));
                        return;
                    }
                    userRepository.delete(optUser.get().getUuid()).thenRun(() -> {
                        sessionManager.removePlayer(optUser.get().getUuid());
                        sessionManager.invalidateSession(optUser.get().getUuid());
                        source.sendMessage(messageService.parse("<green>Аккаунт " + targetName + " успешно удалён!</green>"));
                    });
                });
            }
            case "setpremium" -> {
                if (args.length < 3) {
                    source.sendMessage(messageService.parse("<yellow>Использование: /auth setpremium <ник> <true|false></yellow>"));
                    return;
                }
                String targetName = args[1];
                boolean isPremium = Boolean.parseBoolean(args[2]);

                userRepository.findByUsername(targetName).thenAccept(optUser -> {
                    if (optUser.isEmpty()) {
                        source.sendMessage(messageService.parse("<red>Пользователь " + targetName + " не найден в базе данных!</red>"));
                        return;
                    }
                    userRepository.setPremium(optUser.get().getUuid(), isPremium).thenRun(() -> {
                        source.sendMessage(messageService.parse("<green>Статус лицензии для " + targetName + " изменён на: " + isPremium + "</green>"));
                    });
                });
            }
            default -> source.sendMessage(messageService.parse("<red>Неизвестная подкоманда! Введите /auth для справки.</red>"));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            return List.of("reload", "unregister", "setpremium");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("setpremium")) {
            return List.of("true", "false");
        }
        return List.of();
    }
}
