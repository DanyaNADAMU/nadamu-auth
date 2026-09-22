package mu.nada.nadamuauth.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import mu.nada.nadamuauth.config.ConfigManager;
import mu.nada.nadamuauth.config.MessagesConfig;
import mu.nada.nadamuauth.security.SessionManager;
import mu.nada.nadamuauth.storage.UserRepository;
import mu.nada.nadamuauth.util.MessageService;
import org.spongepowered.configurate.ConfigurateException;

import java.util.List;
import java.util.Map;

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
            messageService.sendMessage(source, MessagesConfig::adminNoPermission);
            return;
        }

        String[] args = invocation.arguments();
        if (args.length == 0) {
            messageService.sendMessage(source, MessagesConfig::adminHelp);
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> {
                try {
                    configManager.reload();
                    messageService.sendMessage(source, MessagesConfig::adminReloadSuccess);
                } catch (ConfigurateException e) {
                    messageService.sendMessage(source, MessagesConfig::adminReloadError,
                            Map.of("error", e.getMessage() != null ? e.getMessage() : "unknown"));
                }
            }
            case "unregister" -> {
                if (args.length < 2) {
                    messageService.sendMessage(source, MessagesConfig::adminUnregisterUsage);
                    return;
                }
                String targetName = args[1];
                userRepository.findByUsername(targetName).thenAccept(optUser -> {
                    if (optUser.isEmpty()) {
                        messageService.sendMessage(source, MessagesConfig::adminUserNotFound, Map.of("player", targetName));
                        return;
                    }
                    userRepository.delete(optUser.get().getUuid()).thenRun(() -> {
                        sessionManager.removePlayer(optUser.get().getUuid());
                        sessionManager.invalidateSession(optUser.get().getUuid());
                        messageService.sendMessage(source, MessagesConfig::adminUnregisterSuccess, Map.of("player", targetName));
                    });
                });
            }
            case "setpremium" -> {
                if (args.length < 3) {
                    messageService.sendMessage(source, MessagesConfig::adminSetPremiumUsage);
                    return;
                }
                String targetName = args[1];
                boolean isPremium = Boolean.parseBoolean(args[2]);

                userRepository.findByUsername(targetName).thenAccept(optUser -> {
                    if (optUser.isEmpty()) {
                        messageService.sendMessage(source, MessagesConfig::adminUserNotFound, Map.of("player", targetName));
                        return;
                    }
                    userRepository.setPremium(optUser.get().getUuid(), isPremium).thenRun(() -> {
                        messageService.sendMessage(source, MessagesConfig::adminSetPremiumSuccess,
                                Map.of("player", targetName, "status", String.valueOf(isPremium)));
                    });
                });
            }
            default -> messageService.sendMessage(source, MessagesConfig::adminUnknownSubcommand);
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
