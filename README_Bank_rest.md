# Система управления банковскими картами

REST API на Spring Boot для управления банковскими картами: создание, блокировка, переводы между своими картами, просмотр баланса. Есть роли ADMIN и USER, JWT-аутентификация, шифрование номеров карт.

## Технологии

- Java 17, Spring Boot 3
- Spring Security + JWT
- Spring Data JPA, PostgreSQL, Liquibase
- Swagger UI (OpenAPI)
- JUnit 5 + Mockito (тесты)

## Что умеет система

### Администратор (ADMIN)
- Создаёт карты для пользователей
- Блокирует, активирует, удаляет карты
- Смотрит все карты (с пагинацией)
- Управляет пользователями: смена роли, удаление

### Пользователь (USER)
- Смотрит свои карты (фильтр по статусу, пагинация)
- Запрашивает блокировку своей карты
- Делает переводы между своими картами
- Смотрит баланс карты

## Безопасность

- Номера карт шифруются AES перед сохранением в БД
- В ответах API номер маскируется: `**** **** **** 1234`
- Пароли хранятся в виде BCrypt-хеша
- Доступ к эндпоинтам разграничен по ролям
- Ошибки 401/403 возвращаются в JSON, а не в HTML

## Структура проекта

```
com.example.bankcards/
├── controller/   — REST-контроллеры (Auth, Card, Admin)
├── service/      — бизнес-логика (CardService, TransactionService, AdminService, UserService)
├── repository/   — Spring Data JPA репозитории
├── entity/       — JPA-сущности (User, Card, Transaction)
├── dto/          — объекты запросов и ответов (AuthRequest, CardRequest, CardResponse, TransferRequest)
├── exception/    — глобальная обработка ошибок, кастомные исключения
├── config/       — SecurityConfig, SwaggerConfig
├── security/     — JWT-фильтр, JwtService, UserDetailsService
└── util/         — EncryptionService (шифрование карт)
```

## Запуск

### Требования
- Java 17+
- PostgreSQL

### Шаги
1. Создать БД `bank_cards` в PostgreSQL
2. Настроить подключение в `application.yml` (или через переменные окружения)
3. Запустить приложение:
   ```
   mvn spring-boot:run
   ```
4. Liquibase автоматически создаст таблицы при первом запуске
5. Swagger UI доступен по адресу: `http://localhost:8080/swagger-ui.html`

### Переменные окружения

| Переменная | Назначение | По умолчанию |
|------------|------------|--------------|
| `JWT_SECRET` | Секретный ключ для JWT | встроенный |
| `ENCRYPTION_SECRET` | Ключ шифрования карт (AES) | встроенный |
| `ENCRYPTION_IV` | Вектор инициализации (AES) | встроенный |

## Тесты

- **Unit-тесты** (Mockito): EncryptionService, CardService, TransactionService
- **Интеграционные тесты** (MockMvc + БД): AuthController, AdminController, CardController

Запуск:
```
mvn test
```
