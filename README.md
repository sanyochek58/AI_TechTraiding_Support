# AI Trading Signal System

Java-приложение для анализа котировок криптовалютных пар в реальном времени и генерации торговых сигналов (LONG / SHORT / HOLD) на основе технического анализа, с расчётом на дальнейшее подключение собственной ML-модели.

> ⚠️ Работа ведётся на [Binance Testnet](https://testnet.binance.vision/) — тестовой сети биржи без реальных средств. Генерируемые сигналы не являются инвестиционной рекомендацией.

## Возможности

- Получение исторических свечей (klines) с Binance по REST API
- Подписка на живой поток свечей через WebSocket с автоматическим переподключением при разрыве соединения
- Расчёт индикатора SMA (Simple Moving Average)
- Генерация торговых сигналов на основе технического анализа (пересечение цены со скользящей средней)
- Сохранение истории сгенерированных сигналов в локальную базу данных для последующего анализа качества (backtesting)

## Технологический стек

- **Java 21** (LTS)
- **Gradle** (Kotlin DSL)
- **Jackson** — работа с JSON
- **java.net.http.HttpClient / WebSocket** — стандартный Java API для REST и WebSocket
- **SQLite** (через `sqlite-jdbc`) — локальное хранилище истории сигналов
- **JUnit 5** — модульное тестирование
- **GitHub Actions** — CI (автоматическая сборка и прогон тестов при каждом push/PR)

## Архитектура

Проект построен как **модульный монолит**: единое приложение с чёткими внутренними границами между бизнес-доменами, что позволяет в дальнейшем безболезненно выносить отдельные модули в независимые сервисы. Каждый модуль организован по слоям в духе Hexagonal Architecture (Ports & Adapters):

```
com.example.aitraiding
├── marketdata/          Получение и представление рыночных данных
│   ├── domain/           Candle, MarketDataProvider, LiveCandleSubscriber (порты)
│   ├── application/       MarketDataService (оркестрация)
│   └── infrastructure/    BinanceMarketDataClient (REST), BinanceKlineWebSocketClient (WebSocket)
│
├── strategy/             Анализ данных и генерация сигналов
│   ├── domain/            Signal, SignalStrategy (порт), SmaCalculator, SmaCrossoverStrategy
│   ├── application/        SignalGenerationService (буферизация свечей, оркестрация)
│   └── infrastructure/     (зарезервировано под ML-стратегии)
│
├── signalhistory/        Персистентность истории сигналов
│   ├── domain/             SignalRecord, SignalHistoryRepository (порт)
│   ├── application/         SignalHistoryService
│   └── infrastructure/      SqliteSignalHistoryRepository
│
├── console/               Форматированный вывод в консоль
└── Main.java              Точка входа, композиция зависимостей
```

**Принцип разделения слоёв:**
- `domain` — чистая бизнес-логика и контракты (интерфейсы-порты), без зависимостей от внешних технологий.
- `application` — оркестрация use-cases, использует порты из `domain` через Dependency Injection.
- `infrastructure` — реализации портов ("адаптеры"): HTTP, WebSocket, JDBC.

Такое разделение позволяет заменять реализации (другую биржу вместо Binance, PostgreSQL вместо SQLite) без изменения бизнес-логики.

## Начало работы

### Требования

- JDK 21+
- Gradle (в комплекте есть Gradle Wrapper — отдельная установка не требуется)

### Запуск

```bash
git clone https://github.com/sanyochek58/AI_TechTraiding_Support.git
cd AI_TechTraiding_Support
./gradlew build
./gradlew run
```

Приложение подключится к Binance Testnet, получит историю последних свечей по паре `BTCUSDT` (5-минутный интервал), рассчитает SMA, подпишется на живой поток и начнёт генерировать сигналы по мере закрытия новых свечей — с сохранением каждого сигнала в локальную базу `signals.db`.

### Тесты

```bash
./gradlew test
```

## CI

При каждом push в `dev`/`main`, а также при открытии Pull Request в `main`, GitHub Actions автоматически собирает проект и прогоняет полный набор тестов.