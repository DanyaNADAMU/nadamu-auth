package mu.nada.nadamuauth.i18n;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import mu.nada.nadamuauth.config.MessagesConfig;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageManager {

    private final Path languagesDirectory;
    private final Logger logger;
    private final Map<String, MessagesConfig> languages = new ConcurrentHashMap<>();
    private String defaultLanguage = "ru";

    public LanguageManager(Path dataDirectory, Logger logger) {
        this.languagesDirectory = dataDirectory.resolve("languages");
        this.logger = logger;
    }

    public void reload(String defaultLanguage) throws ConfigurateException {
        this.defaultLanguage = (defaultLanguage != null && !defaultLanguage.isBlank())
                ? defaultLanguage.toLowerCase()
                : "ru";

        try {
            if (!Files.exists(languagesDirectory)) {
                Files.createDirectories(languagesDirectory);
            }
        } catch (IOException e) {
            logger.error("Failed to create languages directory: {}", languagesDirectory, e);
        }

        // Generate default language dictionaries if missing
        ensureLanguageFile("ru.yml", new MessagesConfig());
        ensureLanguageFile("en.yml", MessagesConfig.createEnglishDefault());

        // Load all .yml / .yaml files from languages/
        languages.clear();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(languagesDirectory, "*.{yml,yaml}")) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String langCode = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();

                MessagesConfig config = loadLanguageFile(path);
                languages.put(langCode, config);
            }
        } catch (IOException e) {
            logger.error("Error reading language files from {}", languagesDirectory, e);
        }

        logger.info("Loaded {} language(s): {} (default: '{}')",
                languages.size(), languages.keySet(), this.defaultLanguage);
    }

    private void ensureLanguageFile(String fileName, MessagesConfig defaultConfig) throws ConfigurateException {
        Path filePath = languagesDirectory.resolve(fileName);
        if (!Files.exists(filePath)) {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(filePath)
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();

            CommentedConfigurationNode node = loader.load();
            node.set(MessagesConfig.class, defaultConfig);
            loader.save(node);
        }
    }

    private MessagesConfig loadLanguageFile(Path path) throws ConfigurateException {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(path)
                .nodeStyle(NodeStyle.BLOCK)
                .build();

        CommentedConfigurationNode root = loader.load();
        MessagesConfig config = root.get(MessagesConfig.class);
        if (config == null) {
            config = new MessagesConfig();
        }
        return config;
    }

    public MessagesConfig getMessages(CommandSource source) {
        if (source instanceof Player player) {
            return getMessagesForPlayer(player);
        }
        return getDefaultMessages();
    }

    public MessagesConfig getMessagesForPlayer(Player player) {
        try {
            if (player.getPlayerSettings() != null) {
                Locale locale = player.getPlayerSettings().getLocale();
                if (locale != null) {
                    String lang = locale.getLanguage().toLowerCase();
                    if (languages.containsKey(lang)) {
                        return languages.get(lang);
                    }

                    // Check full tag e.g. ru_ru
                    String fullCode = locale.toString().toLowerCase();
                    if (languages.containsKey(fullCode)) {
                        return languages.get(fullCode);
                    }

                    // Check hyphenated tag e.g. ru-ru
                    String tagCode = locale.toLanguageTag().toLowerCase();
                    if (languages.containsKey(tagCode)) {
                        return languages.get(tagCode);
                    }
                }
            }
        } catch (Exception ignored) {
            // Fallback to default if player settings is inaccessible
        }

        return getDefaultMessages();
    }

    public MessagesConfig getDefaultMessages() {
        MessagesConfig config = languages.get(defaultLanguage);
        if (config != null) {
            return config;
        }
        return languages.getOrDefault("ru", languages.values().stream().findFirst().orElseGet(MessagesConfig::new));
    }

    public Map<String, MessagesConfig> getLoadedLanguages() {
        return languages;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
}
