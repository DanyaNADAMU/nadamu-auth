# Руководство по установке и настройке

## Системные требования
- Прокси-сервер Velocity 4.0.0 или новее.
- Среда выполнения Java 25.
- Запущенный инстанс NanoLimbo, зарегистрированный в Velocity.
- Как минимум один игровой бэкенд-сервер (например, Paper Lobby).

## Установка
1. Соберите плагин с помощью команды `./gradlew shadowJar` или скачайте релизный файл `NadamuAuth-<version>.jar`.
2. Скопируйте файл в папку `plugins/` на вашем сервере Velocity.
3. Запустите прокси для автоматической генерации стандартных конфигураций и словарей в папке `plugins/nadamu-auth/`:
   - `config.yml`
   - `languages/ru.yml`
   - `languages/en.yml`

## Настройка Velocity (`velocity.toml`)

В гибридном режиме работы прокси настройте `velocity.toml`:
```toml
# Должно быть false, чтобы Velocity принимал как пиратов, так и лицензии
online-mode = false

[servers]
limbo = "127.0.0.1:25566"
lobby = "127.0.0.1:25567"
pvp = "127.0.0.1:25568"

try = [
  "lobby"
]

[forced-hosts]
"pvp.example.com" = [ "pvp" ]
```

## Настройка плагина (`config.yml`)
```yaml
servers:
  auth-server: "limbo"
  lobby-server: "lobby"

database:
  type: "H2"
  file-name: "nadamu_auth"
  pool-size: 10

security:
  min-password-length: 6
  max-password-length: 64
  max-login-attempts: 5
  lockout-minutes: 10
  # Длительность сессии по IP в минутах (1440 = 24 часа, 0 = отключить)
  session-timeout-minutes: 1440
  login-timeout-seconds: 60

guest:
  allow-guest: true
  reminder-interval-seconds: 60

premium:
  enabled: true
  verification-timeout-minutes: 5
  max-failed-attempts: 2

localization:
  # Язык по умолчанию, если язык клиента игрока не найден в languages/
  default-language: "ru"
```

## Локализация (i18n)

Файлы сообщений хранятся в `plugins/nadamu-auth/languages/`.
- Файлы `ru.yml` и `en.yml` создаются автоматически.
- Язык игрока определяется автоматически по настройкам его клиента (`player.getPlayerSettings().getLocale()`).
- Администраторы могут редактировать любые фразы или добавлять файлы других языков (например, `de.yml`, `es.yml`).
- Команда `/auth reload` на лету перезагружает `config.yml` и все языковые файлы без перезапуска сервера.

## Права доступа (Permissions)
- `nadamuauth.admin` — даёт доступ к командам:
  * `/auth reload` (`/nauth reload`)
  * `/auth unregister <ник>` (`/nauth unregister`)
  * `/auth setpremium <ник> <true|false>` (`/nauth setpremium`)
