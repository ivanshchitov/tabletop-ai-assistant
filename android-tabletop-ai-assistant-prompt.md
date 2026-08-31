# ПРОМПТ ДЛЯ СОЗДАНИЯ ANDROID-ПРИЛОЖЕНИЯ "TABLETOP AI ASSISTANT"

> Генерацию выполняет агент с навыком **android-developer**. Строго соблюдай правила из
> `references/rules.md` (архитектура, слои, нейминг, ошибки, DI, тесты, навигация).
> DI-фреймворк для проекта уже выбран — **Hilt**. Никогда не смешивай его с Koin.
> Весь код — рабочий, без TODO и заглушек.

> Этот файл описывает **фактически реализованное** приложение (актуализирован по коду). Подробности
> архитектуры и известные грабли по сборке — в `CLAUDE.md` в корне репозитория; при расхождении между
> этим файлом и кодом ориентируйся на код и `CLAUDE.md`.

---

## НАЗВАНИЕ ПРОЕКТА
**Tabletop AI Assistant** — Android-приложение-ассистент по настольным играм на базе модели
`deepseek-v4-flash`, доступной через OpenAI-совместимый эндпоинт OpenCode Zen.
Это мобильный аналог существующего TUI-приложения `tabletop-ai-assistant-tui`
(см. `tui_app.py`, `api_client.py`, `history_manager.py`, `prompts.py`, `config.py`).

Base package: `org.dishch.tabletopaiassistant`. Один модуль `:app`.

---

## ОБЩАЯ ЗАДАЧА
Разработать Android-приложение **Tabletop AI Assistant**, которое:
- **Внутри себя содержит фиксированный системный промпт** (роль ассистента) и **агентный промпт**
  (логика работы, фильтрация тем, формат ответов) — жёстко зашиты в код, на экране не показываются.
- Принимает от пользователя **только пользовательские промпты** (вопросы по настольным играм)
  через текстовое поле ввода.
- **Строго фильтрует темы**: если вопрос не касается настольных игр — модель вежливо объясняет
  причину отказа и предлагает задать вопрос по теме (фильтрация реализуется агентным промптом).
- Отображает ответы как **отрендеренный Markdown** (заголовки, списки, жирный текст), а не сырой текст.
- Сохраняет **историю диалогов между запусками** (последние 50) и показывает её при старте.
- Обрабатывает все ошибки, как в TUI-версии (см. раздел «Обработка ошибок»).
- **Локализован** на английский (язык по умолчанию) и русский — весь UI-текст берётся из строковых
  ресурсов, переключается системной локалью устройства.

---

## ВСТРОЕННЫЕ ПРОМПТЫ (HARDCODED) — переносятся без изменений

Живут в `feature/assistant/domain/AssistantPrompts.kt`. Это инструкции для LLM, а не UI-текст — они
**не локализуются** и не выносятся в строковые ресурсы, даже несмотря на общее требование локализации UI.

### 1. СИСТЕМНЫЙ ПРОМПТ (определяет роль)
```
Ты — эксперт по настольным играм (board games). Твоя специализация — правила, стратегии, разрешение спорных ситуаций, рекомендации и история настольных игр. Ты всегда отвечаешь вежливо, структурированно и по делу. Твоё имя — Tabletop AI Assistant.
```

### 2. АГЕНТНЫЙ ПРОМПТ (определяет поведение и фильтрацию)
```
Твоя задача — обрабатывать только запросы, связанные с настольными играми.

Правила:
1. Если пользователь спрашивает о чём-либо, не относящемся к настольным играм (кино, еда, политика, технологии, личные вопросы и т.д.), ТЫ ОБЯЗАН ответить строго одной фразой:
   "Я не могу рассказать об этом, потому что я — Tabletop AI Assistant, и я работаю только с вопросами по настольным играм. Пожалуйста, задайте вопрос по теме."
   Никаких дополнительных пояснений, советов или переходов на другие темы.

2. Если вопрос касается настольных игр:
   - Отвечай структурированно: краткий ответ → детали → итог.
   - Форматируй ответ в Markdown: заголовки, **жирный текст**, маркированные и нумерованные списки.
   - Используй пункты, нумерацию, смайлики (🎲, 🏆, 🎯, 📖, ♟️) для ключевых моментов.
   - При разрешении спорных правил ссылайся на логику и официальные источники.
   - Для рекомендаций уточняй количество игроков, сложность, длительность, жанр.
   - Если игра неизвестна — предложи похожие игры или уточни описание.

3. Не задавай уточняющих вопросов, если тема не относится к играм — сразу выдавай отказ.
4. Если вопрос по играм, но слишком общий — дай развёрнутый ответ с примерами.
```

Объединение для отправки: `system = SYSTEM_PROMPT + "\n\n" + AGENT_PROMPT`,
`user = "Вопрос пользователя: <текст>"` (см. `AssistantPrompts.buildUserPrompt`).

---

## ТЕХНИЧЕСКИЙ СТЕК (соответствует навыку android-developer)

| Слой | Технология |
|------|-----------|
| Язык | Kotlin |
| UI | Jetpack Compose, Material 3, type-safe Navigation Compose (`NavigationConfig` + `AppNavGraph`) |
| Архитектура | Feature-first, три слоя (`domain` / `data` / `presentation`), UDF/MVI |
| State | `StateFlow<State>` в ViewModel |
| Side-effects | `Channel<SideEffect>` (toast, навигация) |
| DI | **Hilt** — единый для всего проекта, Koin не используется |
| Локальное хранилище | Room (`AppDatabase`, `ChatMessageEntity`, `ChatMessageDao`) — offline-first, source of truth |
| Сетевое хранилище | Retrofit + OkHttp + kotlinx.serialization |
| Хранение API-ключа | `local.properties` (`OPENCODE_API_KEY`) → `BuildConfig.OPENCODE_API_KEY` → `ApiConfig.API_KEY`; читается **только на этапе сборки**, сборка падает (`GradleException`), если ключ пуст или отсутствует |
| Локализация | `res/values/strings.xml` (English, по умолчанию) + `res/values-ru/strings.xml`; Composable — `stringResource()`, ViewModel — `core/resources/ResourceProvider` (не внедряет `Context` напрямую) |
| Асинхронность | Coroutines + Flow; `Dispatchers.IO` не использовать в ViewModel напрямую |
| Рендеринг Markdown | библиотека `com.github.jeziellago:compose-markdown` (compose-markdown, резолвится с JitPack) — для рендеринга ответов |
| Тесты | `mockk`, `kotlinx-coroutines-test` (`runTest`) |

Версии зависимостей — только через `gradle/libs.versions.toml`, по шаблону из `rules.md` §17.

---

## API-ВЗАИМОДЕЙСТВИЕ

- Endpoint: `https://opencode.ai/zen/v1/chat/completions`
- Модель: `deepseek-v4-flash`
- Температура: `0.7`
- Максимальная длина ответа: `1000` токенов
- Таймаут запроса: `30` секунд
- Ретраи: до `3` попыток с экспоненциальной задержкой `2^n` секунд, **только при `SocketTimeoutException`**
  (OkHttp `RetryInterceptor` в `core/network/`) — ошибка соединения (нет сети) ретраев не делает,
  падает сразу с понятным сообщением.

**Формат запроса:**
```json
{
  "model": "deepseek-v4-flash",
  "messages": [
    {"role": "system", "content": "<СИСТЕМНЫЙ_ПРОМПТ>\n\n<АГЕНТНЫЙ_ПРОМПТ>"},
    {"role": "user", "content": "Вопрос пользователя: <текст>"}
  ],
  "temperature": 0.7,
  "max_tokens": 1000
}
```

**DTO (`feature/assistant/data/dto/`):**
- `ChatCompletionRequestDto` — `model`, `messages: List<MessageDto>`, `temperature`, `max_tokens`.
- `ChatCompletionResponseDto` — `choices: List<ChoiceDto>`; `ChoiceDto.message: MessageDto`.
- `MessageDto` — `role: String`, `content: String`.
- `@Serializable` + `@SerialName("snake_case")` по правилам `rules.md` §7. Авторизация — заголовок
  `Authorization: Bearer <ApiConfig.API_KEY>`, добавляется в `AssistantRemoteDataSource`.

---

## ФУНКЦИОНАЛЬНЫЕ ТРЕБОВАНИЯ

### Экран чата (основной, `feature/assistant`)
- Заголовок: **«🎲 Tabletop AI Assistant»** (Material 3 `TopAppBar`).
- Список сообщений: `LazyColumn` со скроллом вниз при новых сообщениях; сообщения пользователя —
  синие «пузыри» справа, ответы ассистента — Markdown слева (акцент фиолетовый).
- Поле ввода вопроса + кнопка «Отправить». Пустой ввод игнорируется.
- Ввод длиннее **2000 символов** → обрезается до 2000 с предупреждением пользователю (toast).
- При старте: если история не пуста — загружается из Room и показывается весь диалог;
  если пуста — показывается приветствие «🎲 Tabletop AI Assistant запущен. Задайте вопрос
  по настольным играм.»
- Статус-строка (под списком): «Готов ✅» / «● Отправка...», счётчик диалогов за сессию.
- Экран использует `Modifier.imePadding()` на корневом `Scaffold` — без него клавиатура перекрывает
  поле ввода, потому что `MainActivity` включает edge-to-edge (`enableEdgeToEdge()`), и одного
  `android:windowSoftInputMode="adjustResize"` в манифесте недостаточно.

### Отправка вопроса
- Во время ожидания ответа — `CircularProgressIndicator` / индикатор «● Отправка...».
- Ответ ассистента появляется с **эффектом постепенной печати** (фрагментами по 3 символа,
  задержка ~15 мс, Markdown рендерится по мере наращивания). Пока идёт анимация, ответ **ещё не
  сохранён** в Room — сохраняется только после её завершения (`SaveAssistantAnswerUseCase`), чтобы
  список сообщений не перескакивал сразу на полный текст.
- После успешного ответа диалог **автоматически сохраняется в Room** (last 50 показывается в списке,
  вся история хранится без ограничения), счётчик сессии инкрементируется через общий для двух фич
  `core/session/SessionStatsHolder`.

### Экран настроек (**отдельная фича** `feature/settings`, не часть `feature/assistant`)
- API-ключ **не редактируется в UI** — только через `local.properties` на этапе сборки (см. раздел
  «Хранение API-ключа» выше), поэтому поля ввода ключа на экране настроек нет.
- Кнопка **«Очистить историю»** (удаляет записи из Room).
- Счётчик «Диалогов всего» и «Диалогов за сессию».
- Кнопка «Назад» к чату (+ back-стрелка в `TopAppBar`).

### Навигация
- Два destination: `Chat` (стартовый) и `Settings`, оба на верхнем уровне `NavHost` (без вложенных
  графов — у каждого экрана свой независимый `hiltViewModel()`, состояние между экранами не шарится
  напрямую).
- `NavigationConfig` — sealed interface; `AppNavGraph` в `navigation/AppNavGraph.kt`.
- Переход в настройки — `AssistantSideEffect.NavigateToSettings` (эффект `AssistantViewModel`, из
  `feature/assistant`). Переход назад — `SettingsSideEffect.NavigateBack` (эффект `SettingsViewModel`,
  из `feature/settings`).

---

## СТРУКТУРА КОДА

```
app/src/main/java/org/dishch/tabletopaiassistant/
├── App.kt                          # @HiltAndroidApp
├── MainActivity.kt                 # @AndroidEntryPoint, enableEdgeToEdge(), setContent { AppNavGraph }
├── navigation/
│   ├── NavigationConfig.kt         # sealed interface: Chat, Settings
│   └── AppNavGraph.kt
├── core/
│   ├── di/                         # DatabaseModule, NetworkModule, ResourceModule — @Module/@InstallIn
│   ├── network/                    # Retrofit/OkHttp setup, ApiConfig (incl. API_KEY из BuildConfig), RetryInterceptor
│   ├── database/                   # AppDatabase, entity/ChatMessageEntity, dao/ChatMessageDao
│   ├── session/                    # SessionStatsHolder — общий in-memory счётчик сессии для 2 фич
│   ├── resources/                  # ResourceProvider/ResourceProviderImpl — строки в ViewModel без Context
│   └── ui/                         # Material 3 theme
├── feature/
│   ├── assistant/                  # экран чата
│   │   ├── data/
│   │   │   ├── dto/                # ChatCompletionRequestDto, ChatCompletionResponseDto, MessageDto, ChoiceDto
│   │   │   ├── datasource/         # AssistantApi (Retrofit), AssistantRemoteDataSource, AssistantLocalDataSource (Room)
│   │   │   ├── mapper/             # AssistantMapper: DTO↔domain, Entity↔domain
│   │   │   └── repository/         # AssistantRepositoryImpl
│   │   ├── domain/
│   │   │   ├── AssistantPrompts.kt # SYSTEM_PROMPT, AGENT_PROMPT (не локализуются)
│   │   │   ├── model/              # ChatMessage(id, role: ChatRole, content, createdAt), ChatRole
│   │   │   ├── error/              # AssistantError (sealed): InvalidApiKey, Timeout, NetworkError, UnknownError
│   │   │   ├── repository/         # AssistantRepository (interface)
│   │   │   └── usecase/            # AskQuestionUseCase, SaveAssistantAnswerUseCase, ObserveHistoryUseCase,
│   │   │                           # ObserveSessionCountUseCase, IncrementSessionCountUseCase
│   │   ├── presentation/
│   │   │   ├── mvi/                # AssistantState, AssistantViewEvent, AssistantSideEffect
│   │   │   ├── viewmodel/          # AssistantViewModel
│   │   │   └── ui/                 # AssistantScreen, AssistantView, component/MessageBubble, component/StatusBar
│   │   └── di/
│   │       └── AssistantModule.kt  # @Binds репозиторий + companion @Provides AssistantApi (retrofit.create)
│   └── settings/                   # экран настроек — своя полная вертикаль, не зависит от feature/assistant
│       ├── data/
│       │   ├── datasource/         # SettingsLocalDataSource (тот же ChatMessageDao из core/database)
│       │   └── repository/         # SettingsRepositoryImpl
│       ├── domain/
│       │   ├── error/              # SettingsError (sealed): UnknownError
│       │   ├── repository/         # SettingsRepository (interface)
│       │   └── usecase/            # ObserveDialogCountUseCase, ObserveSessionCountUseCase, ClearHistoryUseCase
│       ├── presentation/
│       │   ├── mvi/                # SettingsState, SettingsViewEvent, SettingsSideEffect
│       │   ├── viewmodel/          # SettingsViewModel
│       │   └── ui/                 # SettingsScreen, SettingsView
│       └── di/
│           └── SettingsModule.kt   # @Binds репозиторий
└── (res/values/strings.xml, res/values-ru/strings.xml — локализация UI)
```

Обе фичи независимы: `feature/assistant` не импортирует `feature/settings` и наоборот. Общий счётчик
диалогов за сессию, который нужен обоим экранам, живёт в `core/session/SessionStatsHolder` — каждая
фича обращается к нему через свой собственный `ObserveSessionCountUseCase` (имя одинаковое, пакеты
разные). Правила слоёв, ошибок, UseCase, ViewModel, Compose UI, DI — строго по `rules.md` (§2–§16).

---

## DOMAIN

### feature/assistant
- `ChatMessage` — `data class(id: Long, role: ChatRole, content: String, createdAt: Long)`; `ChatRole`:
  `USER` / `ASSISTANT` / `SYSTEM`.
- UseCase:
  - `AskQuestionUseCase` — сохраняет вопрос пользователя, отправляет запрос, возвращает
    `Result<String>` (текст ответа, **ещё не сохранён** — см. «Отправка вопроса»), маппит исключения
    в `AssistantError`.
  - `SaveAssistantAnswerUseCase` — сохраняет ответ ассистента в Room после завершения анимации печати.
  - `ObserveHistoryUseCase` — `Result<Flow<List<ChatMessage>>>` из Room (отображается последние 50).
  - `ObserveSessionCountUseCase` / `IncrementSessionCountUseCase` — чтение/инкремент общего счётчика
    сессии (`core/session/SessionStatsHolder`).
- `AssistantError` (sealed): `InvalidApiKey` (HTTP 401), `Timeout` (после всех ретраев),
  `NetworkError(cause)`, `UnknownError(cause)`. Варианта «ключ не задан» больше нет — это гарантируется
  на этапе сборки (см. «Хранение API-ключа»), рантайм-проверка была бы недостижимым кодом.

### feature/settings
- UseCase:
  - `ObserveDialogCountUseCase` — `Result<Flow<Int>>`, общее число диалогов (`role = 'user'` в Room,
    без ограничения в 50).
  - `ObserveSessionCountUseCase` — тот же `SessionStatsHolder`, что и в `feature/assistant`.
  - `ClearHistoryUseCase` — очищает историю в Room.
- `SettingsError` (sealed): `UnknownError(cause)`.

UseCase-правила по `rules.md` §6 для обеих фич: один публичный `operator fun invoke()`
(`suspend`, если есть side-эффект), `Result<T>` / `Result<Flow<T>>`, try/catch внутри — без исключений,
включая тривиальные обёртки вокруг `core`-синглтонов (`IncrementSessionCountUseCase` тоже возвращает
`Result<Unit>`, а не `Unit`).

---

## PRESENTATION (MVI)

### feature/assistant
- `AssistantState`: `messages: List<ChatMessage>`, `input: String`, `isSending: Boolean`,
  `isTyping: Boolean`, `typingContent: String`, `errorMessage: String?`, `sessionCount: Int`.
- `AssistantViewEvent`: `InputChanged(text)`, `SendQuestion`, `Retry`, `LoadHistory`, `OpenSettings`.
- `AssistantSideEffect`: `ShowToast(message)`, `NavigateToSettings`.
- `AssistantViewModel`: единственный источник state; `onEvent(event)` → private-обработчики.
  Зависимости только через UseCase + `ResourceProvider` (никаких Repository/DataSource/DAO/`Context`
  в ViewModel). Ошибки маппятся в локализованные сообщения по `AssistantError` через
  `ResourceProvider.getString(...)` (`rules.md` §5.3, §9).

### feature/settings
- `SettingsState`: `historyCount: Int`, `sessionCount: Int`.
- `SettingsViewEvent`: `LoadSettings`, `ClearHistory`, `NavigateBack`.
- `SettingsSideEffect`: `ShowToast(message)`, `NavigateBack`.
- `SettingsViewModel`: та же дисциплина, что и у `AssistantViewModel` — только UseCase + `ResourceProvider`.

Обе ViewModel независимы (никакого шаринга инстанса между экранами) — каждый получает свой
`hiltViewModel()` в `AppNavGraph.kt`.

---

## UI (Compose)

- `AssistantScreen` / `SettingsScreen` — тонкие адаптеры: `viewModel` + коллекция state, подписка на
  side-effects через `LaunchedEffect(viewModel) { viewModel.effects.collect { ... } }`.
- `AssistantView` — только layout: `LazyColumn` сообщений, поле ввода, кнопка, статус-строка,
  индикаторы загрузки, `Modifier.imePadding()` на `Scaffold`. `@Preview`.
- `SettingsView` — счётчики (Card), кнопка очистки истории, кнопка «Назад». `@Preview`.
- Сообщение ассистента рендерится через compose-markdown (заголовки, списки, `**жирный**`, смайлики).
- Цветовая схема: пользователь — синий, ассистент — фиолетовый акцент, системные сообщения — жёлтый,
  ошибки — красный (Material `colorScheme.error`), статус-строка — приглушённая (`onSurfaceVariant`).
- Margin-значения кратны 4/8/16/24; `LazyColumn` items с `key = { it.id }`.
- **Все видимые пользователю строки** — только `stringResource(R.string....)`, никаких строковых
  литералов в Composable. Единственное исключение — `AssistantPrompts` (инструкции для LLM, не UI).

---

## ЛОГИКА РАБОТЫ

1. **Запуск приложения**:
   - Наличие API-ключа гарантировано на этапе сборки (Gradle-таск падает без него) — рантайм-проверки
     и экрана «добавьте ключ» больше нет.
   - Загружается история из Room (последние 50) и показывается в `LazyColumn`.
   - Если история пуста — приветствие.
2. **Отправка вопроса**:
   - Пустой ввод игнорируется.
   - Ввод > 2000 символов → обрезка + предупреждение (toast).
   - Сообщение пользователя добавляется в список, `isSending = true`, отправляется запрос с объединённым
     системным + агентным промптом.
   - При успехе — ответ появляется с эффектом печати, затем сохраняется в Room, счётчик сессии
     инкрементируется через `core/session/SessionStatsHolder`.
   - При ошибке — `errorMessage`, предложение повторить (`Retry`).
3. **Настройки**: очистка истории удаляет записи из Room — это сразу видно и в чате (`feature/assistant`
   слушает тот же `ChatMessageDao` через свой независимый datasource, Room сам инвалидирует оба потока).
4. **Сворачивание/уничтожение процесса**: история уже в Room, восстанавливается при следующем запуске.

---

## ОБРАБОТКА ОШИБОК

- **Нет / пустой API-ключ** → ошибка **сборки** (`GradleException` в `app/build.gradle.kts`), приложение
  с пустым ключом не соберётся. Рантайм-обработки этого случая в коде нет и не должно быть.
- **HTTP 401 (неверный ключ)** → «Неверный API-ключ.»
- **Таймаут** → до 3 попыток с экспоненциальным backoff, затем «Превышено время ожидания ответа.»
- **Ошибка соединения** → «Ошибка соединения. Проверьте интернет-соединение.» (без ретраев).
- **Прочие HTTP / некорректный ответ** → понятные сообщения (`UnknownError`).
- **Пустой ввод** → игнорируется.
- **Ввод > 2000 символов** → предупреждение и обрезка.
- Ошибки показываются в списке сообщений как системные (красные), не исчезают при следующем действии
  (очищаются только при успешном ответе).
- Все тексты ошибок — локализованные строковые ресурсы, резолвятся во ViewModel через `ResourceProvider`.

---

## ТЕСТЫ

Соответствие 1:1 — на каждый UseCase есть свой `<UseCase>Test.kt`, включая тривиальные обёртки:

- `feature/assistant/domain/usecase/`: `AskQuestionUseCaseTest` (success, 401→`InvalidApiKey`,
  таймаут→`Timeout`, сеть→`NetworkError`), `SaveAssistantAnswerUseCaseTest`,
  `ObserveHistoryUseCaseTest`, `ObserveSessionCountUseCaseTest`, `IncrementSessionCountUseCaseTest`.
- `feature/settings/domain/usecase/`: `ClearHistoryUseCaseTest`, `ObserveDialogCountUseCaseTest`,
  `ObserveSessionCountUseCaseTest`.
- `feature/assistant/presentation/`: `AssistantViewModelTest` — `SendQuestion` переводит state в
  `isSending`, успех сохраняет ответ и инкрементирует счётчик сессии, failure выставляет `errorMessage`.
- `feature/settings/presentation/`: `SettingsViewModelTest` — `LoadSettings` заполняет счётчики,
  `ClearHistory` вызывает use case, `NavigateBack` шлёт эффект.
- Стек: `mockk` + `kotlinx-coroutines-test` (`runTest`, `StandardTestDispatcher`).

---

## ЗАПУСК ПРОЕКТА

```bash
# local.properties (не коммитится) должен содержать:
#   sdk.dir=/path/to/Android/sdk
#   OPENCODE_API_KEY=<ваш ключ>
# Без OPENCODE_API_KEY сборка падает на этапе конфигурации Gradle — это ожидаемо.

./gradlew assembleDebug          # сборка debug APK
./gradlew testDebugUnitTest      # unit-тесты
./gradlew installDebug           # установка на подключённый эмулятор/устройство
```

---

## КЛЮЧЕВЫЕ ОСОБЕННОСТИ (ИТОГ)

| Особенность | Реализация |
|-------------|-----------|
| **Название** | Tabletop AI Assistant |
| **Системный промпт** | Жёстко зашит в коде (роль эксперта), не локализуется |
| **Агентный промпт** | Жёстко зашит в коде (фильтрация тем, формат ответов, требование Markdown), не локализуется |
| **Фильтрация тем** | Мягкий, но строго фиксированный отказ с объяснением причины при вопросах не по теме |
| **API** | `deepseek-v4-flash` через `https://opencode.ai/zen/v1/chat/completions` |
| **UI** | Jetpack Compose + Material 3, экраны Chat (`feature/assistant`) и Settings (`feature/settings`) |
| **Вывод ответов** | Markdown-рендеринг (compose-markdown) с эффектом постепенной печати |
| **История** | Room, offline-first, отображаются последние 50, автосохранение после каждого ответа |
| **API-ключ** | `local.properties` → `BuildConfig` на этапе сборки; сборка падает без ключа; в UI не редактируется |
| **Архитектура** | Feature-first: `feature/assistant` + `feature/settings`, независимые друг от друга; общее — только через `core/` |
| **DI** | Hilt (единый для всего проекта) |
| **Локализация** | `strings.xml` (en по умолчанию) + `strings.xml` (ru), `ResourceProvider` для ViewModel |
| **Обработка ошибок** | Отсутствие ключа — ошибка сборки; 401, таймаут с ретраями 3×backoff, сеть, пустой/длинный ввод — в рантайме |
| **Тесты** | UseCase 1:1 + оба ViewModel (mockk, runTest) |
