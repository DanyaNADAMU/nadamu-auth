package mu.nada.nadamuauth.config;

import mu.nada.nadamuauth.i18n.LanguageManager;
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
    private final LanguageManager languageManager;
    private final Logger logger;

    private PluginConfig config;
    private final Path configPath;

    public ConfigManager(Path dataDirectory, Logger logger) {
        this(dataDirectory, new LanguageManager(dataDirectory, logger), logger);
    }

    public ConfigManager(Path dataDirectory, LanguageManager languageManager, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.languageManager = languageManager;
        this.logger = logger;
        this.configPath = dataDirectory.resolve("config.yml");
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
        this.languageManager.reload(this.config.localization().defaultLanguage());
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

    public LanguageManager languageManager() {
        return languageManager;
    }
}
