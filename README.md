# Tabletop AI Assistant

An Android assistant app for board games. Ask it a rules question, a strategy question, or "what should
I play tonight?" and it answers using an LLM behind the scenes — while politely declining anything that
isn't about board games.

## Features

- 🎲 **Board-game Q&A** — rules, strategy, dispute resolution, and recommendations, answered via
  `deepseek-v4-flash` through the OpenCode Zen chat completions API.
- 🚫 **On-topic only** — a fixed agent prompt filters out anything unrelated to board games and replies
  with a polite, single-sentence refusal instead.
- ✍️ **Markdown answers with a typing effect** — responses render as Markdown (headings, bold, lists) and
  reveal a few characters at a time instead of popping in all at once.
- 💾 **Persistent history** — the last 50 messages are shown on launch and survive app restarts (Room).
- 📊 **Session & lifetime dialog counters** — visible on the chat status bar and the settings screen.
- 🧹 **Clear history** from the settings screen.
- 🌐 **Localized UI** — English (default) and Russian, switching with the device locale.
- ⚠️ **Clear error handling** — missing/invalid API key, timeouts (retried with exponential backoff), and
  connection errors each get a distinct, retryable message.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Architecture | Feature-first, three-layer (`domain`/`data`/`presentation`) + MVI |
| DI | Hilt |
| Local storage | Room |
| Networking | Retrofit, OkHttp, kotlinx.serialization |
| Markdown rendering | [compose-markdown](https://github.com/jeziellago/compose-markdown) |
| Async | Kotlin Coroutines & Flow |
| Tests | JUnit, MockK, kotlinx-coroutines-test |

See [`CLAUDE.md`](CLAUDE.md) for the full architecture write-up (layer boundaries, DI wiring, how the two
features share state, localization conventions, and a few Gradle/AGP version gotchas worth knowing before
you touch the build files).

## Getting started

### Prerequisites

- Android Studio (or the command-line SDK tools) with **SDK Platform 36** installed.
- JDK 17.
- An API key for the OpenCode Zen chat completions endpoint.

### Setup

1. Clone the repo and open it in Android Studio, or use the Gradle wrapper directly from a terminal.
2. Create (or edit) `local.properties` in the project root — it's gitignored — with:

   ```properties
   sdk.dir=/path/to/your/Android/sdk
   OPENCODE_API_KEY=your-api-key-here
   ```

   The build reads `OPENCODE_API_KEY` at configuration time and **fails immediately** (for every Gradle
   task, not just `assemble`) if it's missing or blank — there's no in-app way to set the key, so this file
   must exist before you build.

### Build & run

```bash
./gradlew assembleDebug     # build the debug APK
./gradlew installDebug      # install on a running emulator/device
```

### Test

```bash
./gradlew testDebugUnitTest                                   # all unit tests
./gradlew testDebugUnitTest --tests "*.AskQuestionUseCaseTest" # a single test class
```

## Project structure

```
app/src/main/java/org/dishch/tabletopaiassistant/
├── App.kt, MainActivity.kt
├── navigation/              # NavigationConfig, AppNavGraph
├── core/                    # shared: network, database, DI, session state, resources, theme
└── feature/
    ├── assistant/           # chat screen — domain / data / presentation / di
    └── settings/            # settings screen — domain / data / presentation / di
```

Each feature owns its full stack (domain → data → presentation) and never imports from the other feature
directly; anything they need to share (e.g. the session dialog counter) lives in `core/`.

## License

MIT — see [`LICENSE`](LICENSE).
