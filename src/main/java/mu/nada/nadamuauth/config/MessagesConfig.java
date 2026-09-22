package mu.nada.nadamuauth.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class MessagesConfig {

    @Comment("Префикс сообщений плагина")
    private String prefix = "<dark_gray>[<gradient:#00c6ff:#0072ff>NadamuAuth</gradient>]</dark_gray> ";

    @Comment("Напоминание для гостя о необходимости регистрации")
    private String guestReminder = "<yellow>Ваш аккаунт не защищён! Зарегистрируйтесь: <click:suggest_command:'/register '><aqua>/register <пароль> <повтор></aqua></click></yellow>";

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

    @Comment("Успешная смена пароля")
    private String passwordChanged = "<green>Пароль успешно изменён!</green>";

    @Comment("Смена пароля: старый пароль неверный")
    private String wrongOldPassword = "<red>Текущий пароль указан неверно!</red>";

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

    public String passwordChanged() {
        return passwordChanged;
    }

    public String wrongOldPassword() {
        return wrongOldPassword;
    }
}
