package mu.nada.nadamuauth;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import mu.nada.nadamuauth.commands.*;
import mu.nada.nadamuauth.config.ConfigManager;
import mu.nada.nadamuauth.config.PluginConfig;
import mu.nada.nadamuauth.i18n.LanguageManager;
import mu.nada.nadamuauth.listeners.AuthReminderTask;
import mu.nada.nadamuauth.listeners.ConnectionListener;
import mu.nada.nadamuauth.listeners.RestrictionListener;
import mu.nada.nadamuauth.security.PasswordService;
import mu.nada.nadamuauth.security.RateLimiter;
import mu.nada.nadamuauth.security.SessionManager;
import mu.nada.nadamuauth.service.AuthService;
import mu.nada.nadamuauth.service.LocalAuthService;
import mu.nada.nadamuauth.service.RoutingService;
import mu.nada.nadamuauth.storage.DatabaseManager;
import mu.nada.nadamuauth.storage.UserRepository;
import mu.nada.nadamuauth.util.MessageService;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurateException;

import java.nio.file.Path;
import java.time.Duration;

@Plugin(
        id = "nadamu-auth",
        name = "NadamuAuth",
        version = "1.0.0",
        authors = {"DanyaNADAMU"},
        description = "Lightweight authentication plugin for Velocity proxy"
)
public class NadamuAuthPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ConfigManager configManager;
    private MessageService messageService;
    private DatabaseManager databaseManager;
    private UserRepository userRepository;
    private PasswordService passwordService;
    private RateLimiter rateLimiter;
    private SessionManager sessionManager;
    private AuthService authService;

    @Inject
    public NadamuAuthPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing NadamuAuth...");

        // 1. Configuration and messages
        LanguageManager languageManager = new LanguageManager(dataDirectory, logger);
        this.configManager = new ConfigManager(dataDirectory, languageManager, logger);
        try {
            this.configManager.reload();
            logger.info("Configuration loaded successfully.");
        } catch (ConfigurateException e) {
            logger.error("Failed to load configuration files!", e);
            return;
        }

        PluginConfig config = configManager.config();
        this.messageService = new MessageService(languageManager);

        // 2. Database and repository initialization
        this.databaseManager = new DatabaseManager(dataDirectory, config.database(), logger);
        this.databaseManager.initialize();
        this.userRepository = new UserRepository(databaseManager, logger);

        // 3. Security and session services
        this.passwordService = new PasswordService();
        this.rateLimiter = new RateLimiter(config.security().maxLoginAttempts(), config.security().lockoutMinutes());
        this.sessionManager = new SessionManager(config.security().sessionTimeoutMinutes());

        // 4. Authentication service
        this.authService = new LocalAuthService(
                userRepository,
                passwordService,
                rateLimiter,
                sessionManager,
                config.security(),
                logger
        );

        // 5. Routing service
        RoutingService routingService = new RoutingService(server, config, sessionManager);

        // 6. Register event listeners
        EventManager eventManager = server.getEventManager();
        eventManager.register(this, new ConnectionListener(
                this,
                server,
                config,
                userRepository,
                sessionManager,
                routingService,
                messageService
        ));
        eventManager.register(this, new RestrictionListener(
                config,
                sessionManager,
                messageService
        ));

        // 7. Register proxy commands
        registerCommands(config, routingService);

        // 8. Validate configured servers
        validateServerConfig(config);

        // 9. Background reminder task (for guests and pending logins)
        int reminderInterval = config.guest().reminderIntervalSeconds();
        server.getScheduler()
                .buildTask(this, new AuthReminderTask(server, sessionManager, messageService))
                .repeat(Duration.ofSeconds(reminderInterval))
                .schedule();

        logger.info("NadamuAuth has been initialized successfully!");
    }

    private void registerCommands(PluginConfig config, RoutingService routingService) {
        CommandManager cm = server.getCommandManager();

        CommandMeta loginMeta = cm.metaBuilder("login").aliases("l").plugin(this).build();
        cm.register(loginMeta, new LoginCommand(authService, sessionManager, rateLimiter, routingService, messageService));

        CommandMeta registerMeta = cm.metaBuilder("register").aliases("r").plugin(this).build();
        cm.register(registerMeta, new RegisterCommand(authService, sessionManager, routingService, config, messageService));

        CommandMeta changePasswordMeta = cm.metaBuilder("changepassword").plugin(this).build();
        cm.register(changePasswordMeta, new ChangePasswordCommand(authService, sessionManager, messageService));

        CommandMeta premiumMeta = cm.metaBuilder("premium").plugin(this).build();
        cm.register(premiumMeta, new PremiumCommand(sessionManager, config, messageService));

        CommandMeta adminMeta = cm.metaBuilder("auth").aliases("nauth").plugin(this).build();
        cm.register(adminMeta, new AdminAuthCommand(configManager, userRepository, sessionManager, messageService));
    }

    private void validateServerConfig(PluginConfig config) {
        String authServer = config.servers().authServer();
        String lobbyServer = config.servers().lobbyServer();

        if (server.getServer(authServer).isEmpty()) {
            logger.warn("Configured authServer '{}' is not registered in velocity.toml! Available servers: {}",
                    authServer, server.getAllServers().stream().map(s -> s.getServerInfo().getName()).toList());
        }

        if (server.getServer(lobbyServer).isEmpty()) {
            logger.warn("Configured lobbyServer '{}' is not registered in velocity.toml! Available servers: {}",
                    lobbyServer, server.getAllServers().stream().map(s -> s.getServerInfo().getName()).toList());
        }

        if (authServer.equalsIgnoreCase(lobbyServer)) {
            logger.info("authServer is set to lobbyServer ('{}'). Running in single-server authentication mode.", authServer);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Shutting down NadamuAuth...");
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public mu.nada.nadamuauth.i18n.LanguageManager getLanguageManager() {
        return configManager != null ? configManager.languageManager() : null;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public AuthService getAuthService() {
        return authService;
    }
}
