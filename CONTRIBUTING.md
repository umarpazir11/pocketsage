# Contributing to PocketSage

Thanks for your interest. PocketSage is a fully offline on-device RAG app — contributions that keep it legible and genuinely offline are most valuable.

---

## Ground rules

- **Kotlin only.** Zero Java, zero exceptions.
- **No `INTERNET` permission.** Any PR that adds it will be closed immediately.
- **No `GlobalScope` or `runBlocking` in production code.**
- **No layer bleed.** `domain/` must not import `android.*`. Data sources must not leak into ViewModels directly.
- **No mocks in tests.** Use Fakes (see [Testing](#testing)).

---

## Architecture at a glance

```
data/       — Room, TFLite, MediaPipe, PDF parser. All I/O lives here.
domain/     — UseCases, Repository interfaces, pure Kotlin models. No Android imports.
ui/         — Compose screens + ViewModels. One ViewModel per screen.
di/         — Hilt @Module / @InstallIn only.
```

Errors never throw across layer boundaries — repositories return `Result<T>`. ViewModels map that to a `sealed interface UiState` (`Idle | Loading | Success | Error`).

Dispatchers: `Dispatchers.IO` in data layer, `Dispatchers.Default` for CPU-heavy embedding work.

---

## Setting up

```bash
git clone https://github.com/umerdilpazir/pocketsage.git
cd pocketsage
```

You need two model files not checked into the repo:

| File | Size | Where to put it |
|---|---|---|
| `all-MiniLM-L6-v2.tflite` + `vocab.txt` | ~22 MB | `app/src/main/assets/embedding/` |
| `gemma2b.task` | ~1.3 GB | Side-loaded on device; picked at first run |

Then open in Android Studio or run:

```bash
./gradlew assembleDebug
```

---

## Making a change

1. **Open an issue first** for anything non-trivial (new feature, architectural change, new dependency). A short design sketch saves everyone time.
2. Fork the repo, create a branch off `master`:
   ```bash
   git checkout -b feat/my-change
   ```
3. Make your change. Keep it scoped — one concern per PR.
4. Run the full local check suite before pushing:
   ```bash
   ./gradlew check                    # ktlint + detekt + unit tests
   ./gradlew testDebugUnitTest        # unit tests only
   ./gradlew connectedDebugAndroidTest  # instrumented tests (needs a device/emulator)
   ```
5. Open a **draft PR** early. Reviews are collaborative here.

---

## Commit style

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add cross-encoder re-ranker over top-20 chunks
fix: prevent cosine NaN when embedding vector is all-zero
docs: clarify chunk overlap in README
refactor: extract PromptBuilder from ChatViewModel
test: add fake EmbeddingRepository for SearchDocumentsUseCase
```

One subject line, imperative mood, ≤ 72 characters. Add a body if the *why* isn't obvious.

---

## Testing

- **Unit tests** live in `src/test/` — use Fakes, not Mocks. A Fake implements the same interface as the real class with in-memory state.
- **Instrumented tests** live in `src/androidTest/` — use Room's in-memory database builder.
- Use [Turbine](https://github.com/cashapp/turbine) for testing `Flow` emissions.
- Never use `runBlocking` in tests — use `runTest` from `kotlinx-coroutines-test`.

---

## PR checklist

Before marking your PR ready for review:

- [ ] `./gradlew check` passes with no new warnings
- [ ] New public classes/functions have a one-line KDoc if the purpose isn't obvious from the name
- [ ] No new `android.*` imports in `domain/`
- [ ] No new network permissions in `AndroidManifest.xml`
- [ ] Tests cover the happy path and at least one error path for any new UseCase
- [ ] Commit messages follow Conventional Commits

---

## License

By contributing you agree that your work is licensed under the project's [MIT License](LICENSE).