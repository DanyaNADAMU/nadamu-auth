# Руководство по установке и настройке

## Системные требования
- Прокси-сервер Velocity 4.0.0 или новее.
- Среда выполнения Java 25.
- Запущенный инстанс NanoLimbo, зарегистрированный в Velocity.
- Как минимум один игровой бэкенд-сервер (например, Paper Lobby).

## Установка
1. Соберите плагин с помощью команды: `./gradlew shadowJar`.
2. Скопируйте файл `build/libs/NadamuAuth-1.0-SNAPSHOT.jar` в папку `plugins/` на вашем сервере Velocity.
3. Запустите прокси для генерации стандартных конфигураций в папке `plugins/nadamu-auth/`:
   - `config.yml`
   - `messages.yml`

## Настройка Velocity (`velocity.toml`)
Убедитесь, что серверы объявлены в секции серверов:
```toml
[servers]
limbo = "127.0.0.1:25566"
lobby = "127.0.0.1:25567"

try = [
  "lobby"
]
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
  session-timeout-minutes: 1440
  login-timeout-seconds: 60

guest:
  allow-guest: true
  reminder-interval-seconds: 60

premium:
  enabled: true
  verification-timeout-minutes: 5
  max-failed-attempts: 2
```

## Права доступа (Permissions)
- `nadamuauth.admin` — даёт доступ к командам `/auth reload`, `/auth unregister <ник>` и `/auth setpremium <ник> <true|false>`.
