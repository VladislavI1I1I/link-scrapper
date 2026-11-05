![Build](https://github.com/central-university-dev/backend-academy-2025-spring-template/actions/workflows/build.yaml/badge.svg)

# Link Tracker

<!-- этот файл можно и нужно менять -->

Проект сделан в рамках курса Академия Бэкенда.

Приложение для отслеживания обновлений контента по ссылкам.
При появлении новых событий отправляется уведомление в Telegram.

Проект написан на `Java 23` с использованием `Spring Boot 3`.

Проект состоит из 2-х приложений:

* Bot - Telegram-бот: принимает команды пользователя (`/track`, `/untrack`, `/list` и другие) и присылает уведомления об обновлениях
* Scrapper - сервис, который хранит пользователей и отслеживаемые ссылки в PostgreSQL, по расписанию опрашивает GitHub API и StackOverflow API и публикует найденные обновления

### Архитектура

```
Пользователь <-> Telegram <-> Bot --HTTP (команды)--> Scrapper <--> PostgreSQL
                               ^                         |
                               |                         +--> GitHub API, StackOverflow API
                               |                         |
                               +---- Kafka (updates) <---+
```

* Команды пользователя бот передает в Scrapper по HTTP.
* Обновления по ссылкам Scrapper публикует в Kafka-топик `updates`, бот читает их и рассылает пользователям.
* Сообщения, которые бот не смог обработать после повторных попыток, попадают в топик `updates_dlq`.
* Ссылки, теги и фильтры хранятся в PostgreSQL. Работа с базой возможна через Hibernate или через JDBC, схема создается миграциями Liquibase.
* Список отслеживаемых ссылок пользователя кэшируется ботом в Redis.
* Транспорт уведомлений переключается настройкой `app.message-transport` в scrapper: `kafka` (по умолчанию) или `http`.

## Инструкция по запуску проекта:

### Получение API ключа для бота

Перед тем, как запустить проект, необходимо получить API ключ для работы с ботом:
* Наберите в строке поиска телеграм `@BotFather`
* Откройте с ним диалог
* Нажмите `Start`, а после `/newbot`
* Следуйте указаниям бота
* Готово! Вы получили API ключ! Перейдем к следующему шагу

### Настройка переменных среды

#### BotApplication

* Кликните на текущий профиль
* Выберите `edit configurations...`
* Выберите `BotApplication` в открывшемся окне
* Нажмите `Alt + e` или другой hotkey для переменной среды (Либо нажмите modify options и выберите Environmental variables)
* Добавьте env variable `TELEGRAM_TOKEN` (укажите в нем ваш токен для бота)

#### ScrapperApplication

* Откройте переменные среды, совершив аналогичные описанным в предыдущем шаге действия
* Добавьте env variable `GITHUB_TOKEN` (укажите в нем ваш токен для api github)
* Добавьте env variable `SO_ACCESS_TOKEN` (укажите в нем ваш токен для api stackoverflow)
* Можно переходить к запуску, ура!

### Запуск

* Поднимите инфраструктуру (PostgreSQL, Kafka, Redis): `docker compose -f local-environment/scrapper/docker-compose.yaml up -d`
* Запустите `ScrapperApplication`, а затем `BotApplication`
* Всё работает!

### Дополнительно:

в application.yaml, который расположен в модуле scrapper, теперь появилось несколько новых настроек:
* database.type - вы можете выбрать как работать с бд, через orm систему или напрямую через sql запросы. Для этого пропишите orm или sql соответственно
* migrations - вы можете запустить скрипт для прогона миграций по бд в liquibase, для этого напишите run в этом поле

### Дополнительные источники

#### Инструкция по созданию токенов для github api

* https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens

#### Инструкция по созданию токенов для stackoverflow api

* https://habr.com/ru/articles/217753/

