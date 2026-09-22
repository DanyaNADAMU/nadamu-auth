package mu.nada.nadamuauth.config;

import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private final Path dataDirectory;
    private final Logger logger;

    private PluginConfig config;
    private MessagesConfig messages;

    private final Path configPath;
    private final Path messagesPath;

    public ConfigManager(Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.configPath = dataDirectory.resolve("config.yml");
        this.messagesPath = dataDirectory.resolve("messages.yml");
    }

    public void reload() throws ConfigurateException {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
        } catch (IOException e) {
            logger.error("Failed to create plugin data directory: {}", dataDirectory, e);
        }

        this.config = loadConfigFile(configPath, PluginConfig.class, new PluginConfig());
        this.messages = loadConfigFile(messagesPath, MessagesConfig.class, new MessagesConfig());
    }

    private <T> T loadConfigFile(Path path, Class<T> clazz, T defaultInstance) throws ConfigurateException {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(path)
                .nodeStyle(NodeStyle.BLOCK)
                .build();

        CommentedConfigurationNode root = loader.load();
        T instance = root.get(clazz);

        if (instance == null) {
            instance = defaultInstance;
        }

        root.set(clazz, instance);
        loader.save(root);

        return instance;
    }

    public PluginConfig config() {
        return config;
    }

    public MessagesConfig messages() {
        return messages;
    }
}
