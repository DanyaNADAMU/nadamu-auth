package mu.nada.nadamuauth.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class MessagesConfig {

    @Comment("Префикс сообщений плагина")
    private String prefix = "<dark_gray>[<gradient:#00c6ff:#0072ff>NadamuAuth</gradient>]</dark_gray> ";

    @Comment("Напоминание для гостя о необходимости регистрации")
    private String guestReminder = "<yellow>Ваш аккаунт не защищён! Зарегистрируйтесь: <click:suggest_command:'/r '><aqua>/r <пароль> <повтор></aqua></click></yellow>";

    @Comment("Сообщение при входе на NanoLimbo с требованием логина")
    private String loginRequired = "<red>Пожалуйста, войдите в аккаунт: <aqua>/login <пароль></aqua></red>";

    @Comment("Успешный вход")
    private String loginSuccess = "<green>Вы успешно вошли в аккаунт! Приятной игры.</green>";

    @Comment("Успешная регистрация")
    private String registerSuccess = "<green>Вы успешно зарегистрировались! Ваш аккаунт теперь защищён.</green>";

    @Comment("Попытка зарегистрироваться под уже существующим ником")
    private String alreadyRegistered = "<red>Этот никнейм уже зарегистрирован! Войдите: <aqua>/login <пароль></aqua></red>";

    @Comment("Игрок уже авторизован")
    private String alreadyLoggedIn = "<yellow>Вы уже вошли в аккаунт!</yellow>";

    @Comment("Использование команды регистрации")
    private String registerUsage = "<yellow>Использование: <click:suggest_command:'/r '><aqua>/r <пароль> <повтор></aqua></click></yellow>";

    @Comment("Пароли не совпадают при регистрации")
    private String passwordsDoNotMatch = "<red>Пароли не совпадают! Попробуйте снова.</red>";

    @Comment("Пароль слишком короткий")
    private String passwordTooShort = "<red>Пароль слишком короткий! Минимальная длина: <yellow>{min}</yellow> символов.</red>";

    @Comment("Пароль слишком длинный")
    private String passwordTooLong = "<red>Пароль слишком длинный! Максимальная длина: <yellow>{max}</yellow> символов.</red>";

    @Comment("Неверный пароль")
    private String wrongPassword = "<red>Неверный пароль! Осталось попыток: <yellow>{left}</yellow>.</red>";

    @Comment("Превышен лимит попыток входа")
    private String lockoutMessage = "<red>Слишком много неудачных попыток! Попробуйте через <yellow>{minutes}</yellow> мин.</red>";

    @Comment("Кик по таймауту на сервере авторизации")
    private String timeoutKick = "<red>Время на авторизацию истекло. Пожалуйста, перезайдите на сервер.</red>";

    @Comment("Запрос на подтверждение команды /premium")
    private String premiumPrompt = "<yellow>Вы запросили привязку лицензии Mojang. "
            + "У вас есть <gold>{minutes} мин.</gold> и <gold>{attempts} попытки</gold>, "
            + "чтобы перезайти с лицензионного лаунчера. "
            + "Подтвердите: <click:run_command:'/premium confirm'><aqua>[Нажмите сюда для подтверждения]</aqua></click></yellow>";

    @Comment("Успешное подтверждение лицензии")
    private String premiumSuccess = "<green>Лицензия Mojang успешно подтверждена! Теперь вход будет автоматическим.</green>";

    @Comment("Неудача подтверждения лицензии")
    private String premiumFailed = "<red>Не удалось подтвердить лицензию Mojang. Режим сброшен на оффлайн.</red>";

    @Comment("Поддержка лицензий отключена в конфиге")
    private String premiumDisabled = "<red>Поддержка лицензий Mojang временно отключена администратором.</red>";

    @Comment("Игрок уже играет с подтверждённой лицензией")
    private String premiumAlreadyActive = "<green>Вы уже успешно играете через подтверждённую лицензию Mojang!</green>";

    @Comment("Заявка на лицензию создана")
    private String premiumStarted = "<green>Заявка создана! Пожалуйста, перезайдите на сервер с лицензионного лаунчера в течение <yellow>{minutes}</yellow> мин. (у вас <yellow>{attempts}</yellow> попытки).</green>";

    @Comment("Использование команды смены пароля")
    private String changePasswordUsage = "<yellow>Использование: <aqua>/changepassword <старыйПароль> <новыйПароль></aqua></yellow>";

    @Comment("Успешная смена пароля")
    private String passwordChanged = "<green>Пароль успешно изменён!</green>";

    @Comment("Смена пароля: старый пароль неверный")
    private String wrongOldPassword = "<red>Текущий пароль указан неверно!</red>";

    @Comment("Команда доступна только игрокам")
    private String onlyPlayers = "<red>Эта команда доступна только игрокам!</red>";

    @Comment("Внутренняя ошибка")
    private String internalError = "<red>Произошла внутренняя ошибка. Попробуйте позже.</red>";

    @Comment("Админ: нет прав")
    private String adminNoPermission = "<red>У вас нет прав на использование этой команды!</red>";

    @Comment("Админ: справка по командам")
    private String adminHelp = "<yellow>Команды NadamuAuth:</yellow><br>"
            + "<aqua>/auth reload</aqua> - Перезагрузить конфигурацию<br>"
            + "<aqua>/auth unregister <ник></aqua> - Удалить аккаунт игрока<br>"
            + "<aqua>/auth setpremium <ник> <true|false></aqua> - Установить статус лицензии";

    @Comment("Админ: конфиг перезагружен")
    private String adminReloadSuccess = "<green>Конфигурация и сообщения успешно перезагружены!</green>";

    @Comment("Админ: ошибка перезагрузки конфига")
    private String adminReloadError = "<red>Ошибка при перезагрузке конфигурации: {error}</red>";

    @Comment("Админ: игрок не найден в базе")
    private String adminUserNotFound = "<red>Пользователь {player} не найден в базе данных!</red>";

    @Comment("Админ: использование unregister")
    private String adminUnregisterUsage = "<yellow>Использование: /auth unregister <ник></yellow>";

    @Comment("Админ: аккаунт успешно удалён")
    private String adminUnregisterSuccess = "<green>Аккаунт {player} успешно удалён!</green>";

    @Comment("Админ: использование setpremium")
    private String adminSetPremiumUsage = "<yellow>Использование: /auth setpremium <ник> <true|false></yellow>";

    @Comment("Админ: статус лицензии изменён")
    private String adminSetPremiumSuccess = "<green>Статус лицензии для {player} изменён на: {status}</green>";

    @Comment("Админ: неизвестная подкоманда")
    private String adminUnknownSubcommand = "<red>Неизвестная подкоманда! Введите /auth для справки.</red>";

    public static MessagesConfig createEnglishDefault() {
        MessagesConfig en = new MessagesConfig();
        en.prefix = "<dark_gray>[<gradient:#00c6ff:#0072ff>NadamuAuth</gradient>]</dark_gray> ";
        en.guestReminder = "<yellow>Your account is not protected! Register with: <click:suggest_command:'/r '><aqua>/r <password> <confirm></aqua></click></yellow>";
        en.loginRequired = "<red>Please log in: <aqua>/login <password></aqua></red>";
        en.loginSuccess = "<green>Successfully logged in! Have fun.</green>";
        en.registerSuccess = "<green>Successfully registered! Your account is now protected.</green>";
        en.alreadyRegistered = "<red>This username is already registered! Please log in: <aqua>/login <password></aqua></red>";
        en.alreadyLoggedIn = "<yellow>You are already logged in!</yellow>";
        en.registerUsage = "<yellow>Usage: <click:suggest_command:'/r '><aqua>/r <password> <confirm></aqua></click></yellow>";
        en.passwordsDoNotMatch = "<red>Passwords do not match! Please try again.</red>";
        en.passwordTooShort = "<red>Password is too short! Minimum length: <yellow>{min}</yellow> characters.</red>";
        en.passwordTooLong = "<red>Password is too long! Maximum length: <yellow>{max}</yellow> characters.</red>";
        en.wrongPassword = "<red>Incorrect password! Attempts remaining: <yellow>{left}</yellow>.</red>";
        en.lockoutMessage = "<red>Too many failed attempts! Try again in <yellow>{minutes}</yellow> min.</red>";
        en.timeoutKick = "<red>Login timeout expired. Please reconnect to the server.</red>";
        en.premiumPrompt = "<yellow>You requested to link your Mojang license. "
                + "You have <gold>{minutes} min</gold> and <gold>{attempts} attempts</gold> "
                + "to reconnect using an official launcher. "
                + "Confirm: <click:run_command:'/premium confirm'><aqua>[Click here to confirm]</aqua></click></yellow>";
        en.premiumSuccess = "<green>Mojang license verified successfully! You will now log in automatically.</green>";
        en.premiumFailed = "<red>Failed to verify Mojang license. Mode reset to offline.</red>";
        en.premiumDisabled = "<red>Mojang license support is currently disabled by administrator.</red>";
        en.premiumAlreadyActive = "<green>You are already playing via a verified Mojang license!</green>";
        en.premiumStarted = "<green>Request created! Please reconnect to the server using an official launcher within <yellow>{minutes}</yellow> min (you have <yellow>{attempts}</yellow> attempts).</green>";
        en.changePasswordUsage = "<yellow>Usage: <aqua>/changepassword <oldPassword> <newPassword></aqua></yellow>";
        en.passwordChanged = "<green>Password changed successfully!</green>";
        en.wrongOldPassword = "<red>Current password is incorrect!</red>";
        en.onlyPlayers = "<red>This command is only available to players!</red>";
        en.internalError = "<red>An internal error occurred. Please try again later.</red>";
        en.adminNoPermission = "<red>You do not have permission to use this command!</red>";
        en.adminHelp = "<yellow>NadamuAuth commands:</yellow><br>"
                + "<aqua>/auth reload</aqua> - Reload configuration<br>"
                + "<aqua>/auth unregister <player></aqua> - Delete player account<br>"
                + "<aqua>/auth setpremium <player> <true|false></aqua> - Set premium license status";
        en.adminReloadSuccess = "<green>Configuration and messages reloaded successfully!</green>";
        en.adminReloadError = "<red>Failed to reload configuration: {error}</red>";
        en.adminUserNotFound = "<red>User {player} not found in database!</red>";
        en.adminUnregisterUsage = "<yellow>Usage: /auth unregister <player></yellow>";
        en.adminUnregisterSuccess = "<green>Account {player} successfully deleted!</green>";
        en.adminSetPremiumUsage = "<yellow>Usage: /auth setpremium <player> <true|false></yellow>";
        en.adminSetPremiumSuccess = "<green>License status for {player} changed to: {status}</green>";
        en.adminUnknownSubcommand = "<red>Unknown subcommand! Type /auth for help.</red>";
        return en;
    }

    public String prefix() {
        return prefix;
    }

    public String guestReminder() {
        return guestReminder;
    }

    public String loginRequired() {
        return loginRequired;
    }

    public String loginSuccess() {
        return loginSuccess;
    }

    public String registerSuccess() {
        return registerSuccess;
    }

    public String alreadyRegistered() {
        return alreadyRegistered;
    }

    public String alreadyLoggedIn() {
        return alreadyLoggedIn;
    }

    public String registerUsage() {
        return registerUsage;
    }

    public String passwordsDoNotMatch() {
        return passwordsDoNotMatch;
    }

    public String passwordTooShort() {
        return passwordTooShort;
    }

    public String passwordTooLong() {
        return passwordTooLong;
    }

    public String wrongPassword() {
        return wrongPassword;
    }

    public String lockoutMessage() {
        return lockoutMessage;
    }

    public String timeoutKick() {
        return timeoutKick;
    }

    public String premiumPrompt() {
        return premiumPrompt;
    }

    public String premiumSuccess() {
        return premiumSuccess;
    }

    public String premiumFailed() {
        return premiumFailed;
    }

    public String premiumDisabled() {
        return premiumDisabled;
    }

    public String premiumAlreadyActive() {
        return premiumAlreadyActive;
    }

    public String premiumStarted() {
        return premiumStarted;
    }

    public String changePasswordUsage() {
        return changePasswordUsage;
    }

    public String passwordChanged() {
        return passwordChanged;
    }

    public String wrongOldPassword() {
        return wrongOldPassword;
    }

    public String onlyPlayers() {
        return onlyPlayers;
    }

    public String internalError() {
        return internalError;
    }

    public String adminNoPermission() {
        return adminNoPermission;
    }

    public String adminHelp() {
        return adminHelp;
    }

    public String adminReloadSuccess() {
        return adminReloadSuccess;
    }

    public String adminReloadError() {
        return adminReloadError;
    }

    public String adminUserNotFound() {
        return adminUserNotFound;
    }

    public String adminUnregisterUsage() {
        return adminUnregisterUsage;
    }

    public String adminUnregisterSuccess() {
        return adminUnregisterSuccess;
    }

    public String adminSetPremiumUsage() {
        return adminSetPremiumUsage;
    }

    public String adminSetPremiumSuccess() {
        return adminSetPremiumSuccess;
    }

    public String adminUnknownSubcommand() {
        return adminUnknownSubcommand;
    }
}
