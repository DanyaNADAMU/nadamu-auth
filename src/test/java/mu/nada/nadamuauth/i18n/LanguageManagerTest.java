package mu.nada.nadamuauth.i18n;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.PlayerSettings;
import mu.nada.nadamuauth.config.MessagesConfig;
import mu.nada.nadamuauth.util.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LanguageManagerTest {

    @TempDir
    Path tempDir;

    private LanguageManager languageManager;
    private MessageService messageService;

    @BeforeEach
    void setUp() throws Exception {
        languageManager = new LanguageManager(tempDir, LoggerFactory.getLogger("TestLogger"));
        languageManager.reload("ru");
        messageService = new MessageService(languageManager);
    }

    @Test
    void testEnsureDefaultsCreated() throws Exception {
        Path ruFile = tempDir.resolve("languages/ru.yml");
        Path enFile = tempDir.resolve("languages/en.yml");

        assertTrue(Files.exists(ruFile), "ru.yml must be automatically generated");
        assertTrue(Files.exists(enFile), "en.yml must be automatically generated");

        Map<String, MessagesConfig> loaded = languageManager.getLoadedLanguages();
        assertTrue(loaded.containsKey("ru"), "Loaded languages must contain 'ru'");
        assertTrue(loaded.containsKey("en"), "Loaded languages must contain 'en'");

        MessagesConfig ruConfig = loaded.get("ru");
        MessagesConfig enConfig = loaded.get("en");

        assertTrue(ruConfig.loginSuccess().contains("Вы успешно вошли"));
        assertTrue(enConfig.loginSuccess().contains("Successfully logged in"));

        String ruYaml = Files.readString(ruFile);
        assertTrue(ruYaml.contains("login-success:"), "ru.yml must contain kebab-case keys");
        assertTrue(ruYaml.contains("admin-help:"), "ru.yml must contain admin keys");
    }

    @Test
    void testPlayerLocaleResolution() {
        Player ruPlayer = createFakePlayer(Locale.forLanguageTag("ru-RU"));
        Player enPlayer = createFakePlayer(Locale.forLanguageTag("en-US"));
        Player unknownPlayer = createFakePlayer(Locale.forLanguageTag("es-ES"));

        assertEquals(languageManager.getLoadedLanguages().get("ru").loginSuccess(),
                languageManager.getMessages(ruPlayer).loginSuccess());

        assertEquals(languageManager.getLoadedLanguages().get("en").loginSuccess(),
                languageManager.getMessages(enPlayer).loginSuccess());

        // Unknown locale falls back to defaultLanguage ('ru')
        assertEquals(languageManager.getLoadedLanguages().get("ru").loginSuccess(),
                languageManager.getMessages(unknownPlayer).loginSuccess());
    }

    @Test
    void testCustomLanguageLoading() throws Exception {
        Path deFile = tempDir.resolve("languages/de.yml");
        String deContent = "login-success: \"<green>Erfolgreich angemeldet!</green>\"\n";
        Files.writeString(deFile, deContent);

        languageManager.reload("ru");

        assertTrue(languageManager.getLoadedLanguages().containsKey("de"));
        Player dePlayer = createFakePlayer(Locale.GERMAN);

        assertEquals("<green>Erfolgreich angemeldet!</green>",
                languageManager.getMessages(dePlayer).loginSuccess());
    }

    @Test
    void testMessageServiceComponentAndPlaceholders() {
        Player ruPlayer = createFakePlayer(Locale.forLanguageTag("ru"));
        Player enPlayer = createFakePlayer(Locale.forLanguageTag("en"));

        Component ruComp = messageService.getComponent(ruPlayer, MessagesConfig::passwordTooShort, Map.of("min", "8"));
        Component enComp = messageService.getComponent(enPlayer, MessagesConfig::passwordTooShort, Map.of("min", "8"));

        String ruPlain = PlainTextComponentSerializer.plainText().serialize(ruComp);
        String enPlain = PlainTextComponentSerializer.plainText().serialize(enComp);

        assertTrue(ruPlain.contains("Минимальная длина: 8"), "RU component should contain Russian placeholder value");
        assertTrue(enPlain.contains("Minimum length: 8"), "EN component should contain English placeholder value");
    }

    private Player createFakePlayer(Locale locale) {
        PlayerSettings settings = (PlayerSettings) Proxy.newProxyInstance(
                PlayerSettings.class.getClassLoader(),
                new Class<?>[]{PlayerSettings.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getLocale")) {
                        return locale;
                    }
                    return null;
                }
        );

        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getPlayerSettings")) {
                        return settings;
                    }
                    return null;
                }
        );
    }
}
