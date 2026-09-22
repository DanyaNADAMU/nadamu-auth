package mu.nada.nadamuauth.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class PluginConfig {

    @Comment("Настройки серверов")
    private ServerSettings servers = new ServerSettings();

    @Comment("Настройки базы данных")
    private DatabaseSettings database = new DatabaseSettings();

    @Comment("Настройки безопасности и паролей")
    private SecuritySettings security = new SecuritySettings();

    @Comment("Настройки гостевого режима (ненавязчивая авторизация)")
    private GuestSettings guest = new GuestSettings();

    @Comment("Настройки проверки лицензии (/premium)")
    private PremiumSettings premium = new PremiumSettings();

    public ServerSettings servers() {
        return servers;
    }

    public DatabaseSettings database() {
        return database;
    }

    public SecuritySettings security() {
        return security;
    }

    public GuestSettings guest() {
        return guest;
    }

    public PremiumSettings premium() {
        return premium;
    }

    @ConfigSerializable
    public static class ServerSettings {
        @Comment("Имя сервера авторизации в velocity.toml (NanoLimbo)")
        private String authServer = "limbo";

        @Comment("Имя основного сервера (Lobby), куда отправляется игрок после логина")
        private String lobbyServer = "lobby";

        public String authServer() {
            return authServer;
        }

        public String lobbyServer() {
            return lobbyServer;
        }
    }

    @ConfigSerializable
    public static class DatabaseSettings {
        @Comment("Тип базы данных: H2")
        private String type = "H2";

        @Comment("Имя файла базы данных (без расширения)")
        private String fileName = "nadamu_auth";

        @Comment("Максимальный размер пула соединений HikariCP")
        private int poolSize = 10;

        public String type() {
            return type;
        }

        public String fileName() {
            return fileName;
        }

        public int poolSize() {
            return poolSize;
        }
    }

    @ConfigSerializable
    public static class SecuritySettings {
        @Comment("Минимальная длина пароля")
        private int minPasswordLength = 6;

        @Comment("Максимальная длина пароля")
        private int maxPasswordLength = 64;

        @Comment("Максимальное количество неудачных попыток входа до блокировки")
        private int maxLoginAttempts = 5;

        @Comment("Длительность временной блокировки после превышения попыток (в минутах)")
        private int lockoutMinutes = 10;

        @Comment("Время жизни сессии по IP (в минутах, 1440 = 24 часа)")
        private int sessionTimeoutMinutes = 1440;

        @Comment("Время в секундах, данное на ввод пароля на NanoLimbo перед киком")
        private int loginTimeoutSeconds = 60;

        public int minPasswordLength() {
            return minPasswordLength;
        }

        public int maxPasswordLength() {
            return maxPasswordLength;
        }

        public int maxLoginAttempts() {
            return maxLoginAttempts;
        }

        public int lockoutMinutes() {
            return lockoutMinutes;
        }

        public int sessionTimeoutMinutes() {
            return sessionTimeoutMinutes;
        }

        public int loginTimeoutSeconds() {
            return loginTimeoutSeconds;
        }
    }

    @ConfigSerializable
    public static class GuestSettings {
        @Comment("Разрешить незарегистрированным игрокам сразу играть как гость")
        private boolean allowGuest = true;

        @Comment("Интервал напоминания о регистрации (в секундах)")
        private int reminderIntervalSeconds = 60;

        public boolean allowGuest() {
            return allowGuest;
        }

        public int reminderIntervalSeconds() {
            return reminderIntervalSeconds;
        }
    }

    @ConfigSerializable
    public static class PremiumSettings {
        @Comment("Включена ли поддержка команды /premium")
        private boolean enabled = true;

        @Comment("Время в минутах, за которое игрок должен перезайти с лицензии для подтверждения")
        private int verificationTimeoutMinutes = 5;

        @Comment("Максимальное количество неудачных входов (с пиратки) до отмены заявки")
        private int maxFailedAttempts = 2;

        public boolean enabled() {
            return enabled;
        }

        public int verificationTimeoutMinutes() {
            return verificationTimeoutMinutes;
        }

        public int maxFailedAttempts() {
            return maxFailedAttempts;
        }
    }
}
