# Распределённая система управления питомцами

Учебный проект, реализующий систему на основе микросервисной архитектуры с
асинхронным взаимодействием через брокер. Система состоит из трёх отдельных приложений:
микросервиса для работы с владельцами, микросервиса для работы с питомцами
и HTTP Web-gateway'а для обработки входящих HTTP-запросов.

## Tech Stack

* **Maven**: автоматизация сборки, управление зависимостями проекта.
* **RabbitMQ**: асинхронная обработка сообщений через очереди.
* **Spring AQMP**: интеграция RabbitMQ с Spring Boot.
* **Spring Web MVC**: реализует паттерн Model — View — Controller при помощи готовых компонентов.
* **Spring Data JPA**: работа с базами данных через репозитории.
* **Spring Security**: сессионная аутентификация (`UsernamePasswordAuthenticationToken`), защита эндпоинтов, `@PreAuthorize`, корректный логаут.
* **PostgreSQL**: реляционная СУБД.
* **Flyway**: миграции и версионирование схемы данных.
* **Docker**: контейнеризация (микросервисы, базы данных, RabbitMQ) через `docker-compose`.

## Запуск проекта

Приложение состоит в сумме из **8 сервисов** (3 микросервиса, 3 базы данных, RabbitMQ, Flyway, pgAdmin).

### Требования
- Docker (версия ≥ 20.10)
- Docker Compose (версия ≥ 2.0)

### Шаги

```bash
# 1. Клонировать репозиторий
git clone https://github.com/annapvasileva/distributed_pet_management_system.git

# 2. Собрать и запустить всё (в корне репозитория)
docker-compose up --build -d
```

Сервисы будут запускаться согласно зависимостям:\
`rabbit → postgres → flyway → gateway/pet/owner`

### Доступ к сервисам

| Cервис              | URL                      | Логин                 | Пароль   |
|---------------------|--------------------------|-----------------------|----------|
| API Gateway         | `http://localhost:8000`  | —                     | –        |
| RabbitMQ Management | `http://localhost:15672` | admin                 | password |
| pgAdmin             | `http://localhost:5008`  | postgres@postgres.com | pass     |

### API

Взаимодействие возможно по адресам:\
`http://localhost:8000/api/cats/`, \
`http://localhost:8000/api/users`, \
`http://localhost:8000/api/owners`.

## Завершение работы

```bash
# Остановить всё
docker-compose down

# Очистить volumes (сброс данных)
docker-compose down -v
```

## Структура проекта

```
.
├── flyway-migrations/      # Миграции БД
├── common-dtos/            # Общие DTO микросервисов
├── gateway-service/        # HTTP Web-gateway
├── pet-service/            # Сервис для работы с питомцами (CRUD операции)
├── owner-service/          # Сервис для работы с хозяевами питомцев (CRUD операции)
├── README.md
└── docker-compose.yml
```

