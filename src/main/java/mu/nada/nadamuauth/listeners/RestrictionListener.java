package mu.nada.nadamuauth.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import mu.nada.nadamuauth.config.PluginConfig;
import mu.nada.nadamuauth.model.AuthState;
import mu.nada.nadamuauth.security.SessionManager;
import mu.nada.nadamuauth.util.MessageService;

import java.util.Set;

public class RestrictionListener {

    private static final Set<String> ALLOWED_COMMANDS = Set.of("login", "l", "register", "r", "reg");

    private final PluginConfig pluginConfig;
    private final SessionManager sessionManager;
    private final MessageService messageService;

    public RestrictionListener(PluginConfig pluginConfig,
                               SessionManager sessionManager,
                               MessageService messageService) {
        this.pluginConfig = pluginConfig;
        this.sessionManager = sessionManager;
        this.messageService = messageService;
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        AuthState state = sessionManager.getAuthState(player.getUniqueId());

        if (state == AuthState.PENDING_LOGIN) {
            String authServer = pluginConfig.servers().authServer();
            // Disallow switching to any server other than the designated authServer (NanoLimbo)
            if (!event.getOriginalServer().getServerInfo().getName().equalsIgnoreCase(authServer)) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
            }
        }
    }

    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) {
            return;
        }

        AuthState state = sessionManager.getAuthState(player.getUniqueId());
        if (state == AuthState.PENDING_LOGIN) {
            String rawCmd = event.getCommand().trim();
            String baseCmd = rawCmd.split("\\s+")[0].toLowerCase();

            if (!ALLOWED_COMMANDS.contains(baseCmd)) {
                event.setResult(CommandExecuteEvent.CommandResult.denied());
                messageService.sendMessage(player, messageService.config().loginRequired());
            }
        }
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        AuthState state = sessionManager.getAuthState(player.getUniqueId());

        if (state == AuthState.PENDING_LOGIN) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            messageService.sendMessage(player, messageService.config().loginRequired());
        }
    }
}
