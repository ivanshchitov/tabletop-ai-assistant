# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Tabletop AI Assistant — a single-module Android app (Kotlin, Jetpack Compose, Material 3) that answers
board-game questions via an OpenAI-compatible chat completion API (`deepseek-v4-flash` through OpenCode
Zen, `https://opencode.ai/zen/v1/chat/completions`). The system and agent prompts (role definition + topic
filtering) are hardcoded in `feature/assistant/domain/AssistantPrompts.kt` and never shown in the UI.
`android-tabletop-ai-assistant-prompt.md` at the repo root is the original generation spec the app was
built from — consult it for the full product requirements (error copy, prompt text, functional behavior)
if a change needs to match the original intent.

## Build, test, run

```bash
./gradlew assembleDebug                 # build the debug APK
./gradlew :app:compileDebugKotlin       # fast compile-only check
./gradlew :app:testDebugUnitTest        # unit tests (JVM, mockk + kotlinx-coroutines-test)
./gradlew :app:testDebugUnitTest --tests "*.AskQuestionUseCaseTest"   # single test class
./gradlew installDebug                  # install on a running emulator/device
```

`ANDROID_HOME` must point at an installed SDK (this repo was built against SDK platform 36).
`local.properties` is gitignored and must exist locally with `sdk.dir=...` **and**
`OPENCODE_API_KEY=<your key>` — `app/build.gradle.kts` reads that key at configuration time and throws a
`GradleException` for *every* Gradle invocation (including `clean` or `tasks`) if it's missing or blank, by
design (the app has no other way to configure the key — see "API key" below). Don't add a fallback or make
that check conditional; if the key isn't there yet, the build is supposed to fail.

Dependency versions live only in `gradle/libs.versions.toml`; never hardcode a version string in a
`build.gradle.kts` file. `compose-markdown` is resolved from JitPack (declared in `settings.gradle.kts`),
everything else from Google/Maven Central.

Every `domain/usecase/` class has a matching `<UseCase>Test.kt` (one-to-one, even for thin wrappers around a
repository or `core/session/SessionStatsHolder` — mock the dependency and assert the `Result`); both
ViewModels have tests for their main event scenarios. Keep that 1:1 UseCase-to-test mapping when adding a
new UseCase rather than treating trivial wrappers as exempt.

### Gradle/AGP/Kotlin version gotchas (discovered the hard way — don't "helpfully" change these)

- AGP is on the 9.x line, which ships **built-in Kotlin support**. `com.android.application` applies Kotlin
  Android support itself — do **not** also apply `org.jetbrains.kotlin.android` unless
  `android.builtInKotlin=false` is set in `gradle.properties`. Applying both without that flag fails with
  `Cannot add extension with name 'kotlin'`.
- KSP (Room, Hilt) is **not** compatible with AGP's built-in Kotlin mode, so the flag is currently unset
  (built-in Kotlin stays on) and `org.jetbrains.kotlin.android` is intentionally **not** in the plugin list.
  If you add a KSP-based processor and hit "KSP is not compatible with Android Gradle Plugin's built-in
  Kotlin", the fix is `android.builtInKotlin=false` in `gradle.properties` *and* re-adding the
  `kotlin-android` plugin — but that pairing has its own Kotlin/AGP version compatibility cliff, so bump
  Kotlin first and re-verify with `./gradlew :app:compileDebugKotlin` rather than assuming it'll work.
- Hilt's Gradle plugin must be a version that supports AGP 9's new extension types (2.60.1+ here) — older
  Hilt plugin versions fail with `Android BaseExtension not found`.
- `compileSdk`/`targetSdk` must stay at 36+: several AndroidX libs (`activity`, `activity-compose`) already
  require compiling against API 36, and `checkDebugAarMetadata` fails loudly if it's lower.

## Architecture

Strict feature-first, three-layer architecture (`domain` / `data` / `presentation`) with MVI on top, per
the `android-dev-skill` rules this project was generated under. There are two feature slices, each with its
own `domain/data/presentation/di`, **no direct dependency on one another** — anything they need to share
goes through `core/`: `feature/assistant/` (the chat screen) and `feature/settings/` (the settings screen:
history/session counters and "clear history").

- **Layer dependency direction**: `presentation → domain ← data`. `domain/` never imports from `data/` or
  `presentation/`, and a feature's `domain/`/`data/` never imports another feature's code. Each feature's
  `data/` implements its own `domain/repository/*Repository` and maps DTOs/Room entities to domain models
  via a `data/mapper/`. UseCases are the only thing ViewModels are allowed to depend on — never a Repository
  or DataSource directly.
- **UseCases** live in each feature's `domain/usecase/`, one per action, return `Result<T>` (or
  `Result<Flow<T>>` for reactive reads), and catch all exceptions internally, mapping them to that feature's
  own sealed error type (`AssistantError` / `SettingsError`). `AskQuestionUseCase` intentionally does
  **not** persist the assistant's answer — see the doc comment there; the caller (`AssistantViewModel`)
  persists it via `SaveAssistantAnswerUseCase` only after the client-side typing animation finishes, so Room
  never jumps straight to the final text mid-animation.
- **State**: each feature has its own `StateFlow<...State>` ViewModel + `Channel`-based
  `effects: Flow<...SideEffect>`, following the MVI shape (`presentation/mvi/`). `AssistantViewModel` and
  `SettingsViewModel` are independent instances, each obtained with a plain `hiltViewModel()` call in
  `navigation/AppNavGraph.kt` (no shared-scoping tricks) — don't reintroduce a shared ViewModel between them.
- **Session/history counters, shared without coupling the features**: "Диалогов за сессию" is shown on
  *both* the chat status bar and the settings screen, and "Диалогов всего" only on settings. Since the two
  features can't depend on each other, the session counter lives in `core/session/SessionStatsHolder.kt` (an
  in-memory `@Singleton` `StateFlow<Int>`) — `AssistantViewModel` increments it after a successful answer via
  `IncrementSessionCountUseCase`, and both `AssistantViewModel` and `SettingsViewModel` observe it via their
  own feature-local `ObserveSessionCountUseCase` (same use case name, different package — that's fine, they
  wrap the same core singleton independently). "Диалогов всего" is settings-only and reads Room directly
  through `feature/settings`'s own `ObserveDialogCountUseCase`/`SettingsLocalDataSource`. If you add another
  cross-feature shared value, follow this pattern: put the shared state in `core/`, never let one feature's
  UseCase/Repository reach into another feature's package.
- **DI**: Hilt only (chosen when the project was generated; do not introduce Koin). Core singletons
  (Retrofit/OkHttp, Room, `SessionStatsHolder`, `ResourceProvider`) are provided in `core/di/` and
  `core/session/`; each feature's repository binding — and any feature-specific Retrofit service interface —
  is in its own `feature/<name>/di/<Name>Module.kt`. Everything else uses constructor `@Inject`. Concretely:
  `core/di/NetworkModule.kt` only builds the generic `Retrofit`/`OkHttpClient`; the `AssistantApi` interface
  it serves is created in `feature/assistant/di/AssistantModule.kt`'s companion `@Provides` (it used to live
  in `NetworkModule.kt`, which made `core/` import from `feature/assistant/` — a real rule violation that
  got fixed. Don't move a feature-specific Retrofit service back into `core/di/`).
- **Persistence**: Room (`core/database/`) is the source of truth for chat history, unbounded in storage.
  `feature/assistant`'s `ChatMessageDao.observeLastMessages()` caps what's *displayed* in chat to the last 50
  via a `LIMIT` subquery; `feature/settings`'s own datasource queries the same `ChatMessageDao` directly for
  `observeDialogCount()` (counts `role = 'user'` rows, intentionally uncapped) and `clearHistory()`. Both
  features go through the same `core/database` DAO/table, so a clear from Settings is reflected in Chat's
  live history immediately via Room's own invalidation tracking — no extra wiring needed for that.
- **API key**: read once at build time from `local.properties` (`OPENCODE_API_KEY`) into
  `BuildConfig.OPENCODE_API_KEY`, exposed as `ApiConfig.API_KEY` (`core/network/ApiConfig.kt`), and used
  directly by `AssistantRemoteDataSource` — there is no runtime storage, Settings UI, or "missing key" state
  for it anymore. Because `app/build.gradle.kts` already fails the build when the key is blank, nothing
  downstream (domain, ViewModel, UI) re-checks for a missing/blank key — don't reintroduce that check; it
  would be dead code given the build-time gate already guarantees a non-blank value.
- **Errors → UI**: `AssistantViewModel.mapError()` is the only place `AssistantError` gets turned into
  user-facing copy (localized — see below). Errors are shown as an extra red bubble appended at the end of
  the message list (see `AssistantView.kt`) rather than persisted to Room — they intentionally stay in
  `AssistantState` until the next successful send/retry rather than being cleared automatically.
- **Networking**: retries live in `core/network/RetryInterceptor.kt` and only retry on
  `SocketTimeoutException` (exponential backoff, capped at `ApiConfig.MAX_RETRY_ATTEMPTS`) — a plain
  connection failure (`IOException` that isn't a timeout) is deliberately not retried, matching the product
  spec's distinction between "timeout" and "no connection" error messages.
- **Localization**: every user-facing UI string (screen titles, buttons, placeholders, status text, toasts,
  error copy) is a resource in `res/values/strings.xml` (default = English) and `res/values-ru/strings.xml`
  (Russian) — never a string literal in a Composable or ViewModel. Composables resolve them with
  `stringResource(R.string....)`. ViewModels can't call `stringResource` (not a `@Composable` context), so
  they go through `core/resources/ResourceProvider` (`getString(resId)` / `getString(resId, vararg args)`,
  Hilt-bound to `ResourceProviderImpl` in `core/di/ResourceModule.kt`) instead of injecting `Context`
  directly — keep using that interface (mockable in ViewModel tests) rather than adding a raw `Context`
  dependency to a ViewModel. The two prompts in `feature/assistant/domain/AssistantPrompts.kt`
  (`SYSTEM_PROMPT`/`AGENT_PROMPT`) are a deliberate exception: they are instructions sent to the LLM, not UI
  copy, are never rendered on screen, and stay hardcoded in Russian regardless of app locale — don't move
  them into `strings.xml` or otherwise localize them.
- **Edge-to-edge + IME**: `MainActivity` calls `enableEdgeToEdge()`, which means `android:windowSoftInputMode`
  in the manifest is not enough to keep the keyboard from covering input fields — Compose needs an explicit
  `Modifier.imePadding()` on the screen's root layout (see the `Scaffold` in `AssistantView.kt`). Add the same
  modifier to any new screen that has a text field near the bottom of the layout.
