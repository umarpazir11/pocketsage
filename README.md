<div align="center">

# PocketSage

### A fully offline, on-device RAG Android application powered by LiteRT-LM.
### No servers. No API keys. Complete privacy.

<img src="docs/demo.gif" width="320" alt="PocketSage demo" />

</div>

---

## Why this exists

Most "AI on Android" are thin wrappers around cloud APIs. PocketSage runs the **entire RAG pipeline on the device** — embeddings, vector search, and LLM inference all happen locally on your Android phone. Airplane mode works. Your documents stay on your hardware. The model weights live in your app sandbox.
---

## Features

- **100% offline** — no network calls, ever. Verified by Android's network restrictions. after the one-time model import — no internet permission exists in the app
- **PDF ingestion** — pick any PDF from your device; PocketSage extracts, chunks, and embeds it locally.
- **Semantic search** — LiteRT `all-MiniLM-L6-v2` produces 384-dim embeddings stored as BLOBs in Room.
- **On-device LLM** — Gemma 2B (INT4, ~1.3 GB) running through Google's `LiteRT-LM` (`litertlm-android`) API.
- **Streaming answers** — tokens stream into the UI as they're generated, with retrieved sources shown alongside each answer.
- **Private by design** — model weights and document embeddings live only in your app's sandbox storage.
- 
---

## Architecture

```
PDF  ──►  Extract  ──►  Chunk  ──►  Embed  ──►  Room Store
                                                     │
                                               (cosine search)
                                                     │
User Query  ──►  Embed  ──────────────────────►  Retrieve
                                                     │
                                              Prompt Builder
                                                     │
                                             LiteRT-LM Engine
                                                     │
                                           Token Stream  ──►  Compose UI
```

**Ingestion** — The user picks a PDF via the system file picker. `pdfbox-android` extracts raw text; a sliding-window chunker splits it into ~800-character overlapping segments. Each chunk is embedded by a local MiniLM `.tflite` model (via LiteRT) and stored as a raw 384-float BLOB in Room alongside the chunk text.

**Retrieval** — When a question arrives, the same MiniLM model embeds it. The app loads all stored chunk embeddings from Room, computes cosine similarity against the query vector in-memory (dot product over L2-normalised vectors), and takes the top-K results.

**Generation** — The top chunks are stitched into a prompt template that instructs the model to answer only from the provided context. That prompt is fed to a `LiteRT-LM` `Engine` + `Session`, which streams response tokens back through a `ResponseCallback`. The ViewModel appends each token to a `StateFlow` that the chat composable observes.

The embedding pipeline and the LLM generation pipeline are fully decoupled behind `EmbeddingService` and `LlmRunner` domain interfaces, so either can be swapped without touching the other layer.

---

## Tech Stack

| Layer | Choice | Why |
|---|---|---|
| UI | Jetpack Compose + Material 3 | Modern, declarative native Android UI |
| DI | Hilt 2.58 | First-party, compile-time dependency wiring |
| Storage | Room 2.7.1 | Local structured persistence with BLOB support for embeddings |
| PDF parsing | `pdfbox-android` | Mature on-device text extraction; no network calls |
| Embeddings runtime | LiteRT 1.4.0 | On-device embedding inference; Google's successor to TFLite |
| LLM runtime | LiteRT-LM 0.10.2 (`litertlm-android`) | On-device streaming generation for `.litertlm` models |
| Concurrency | Kotlin Coroutines + Flow | Async ingestion pipelines and real-time token streaming |

---

## How the LLM works

The `.litertlm` model file is stored in app-private storage (`filesDir/models/`), inaccessible to other apps and removed automatically on uninstall.

On launch, the app checks for the model file. If it is absent, the **Model Gate** screen prompts the user to import it via the Storage Access Framework — no sideloading, no `adb` commands required for end users.

Once present, the `LiteRT-LM` engine is initialised like this:

```kotlin
val engine = Engine(
    EngineConfig(
        modelPath = modelRepo.getModelPath().absolutePath,
        cacheDir  = context.cacheDir.absolutePath,
    )
)
engine.initialize()  // executed on a background coroutine — never blocks the main thread
```

`engine.initialize()` is intentionally expensive — it loads and memory-maps the model weights. It is called exactly once (inside a `by lazy` block) and runs on `Dispatchers.IO` to keep the UI thread responsive.

For each user question a new `Session` is created from the live engine. Tokens are streamed back through `ResponseCallback.onNext(response)` and bridged into a Kotlin `Flow<String>` via `callbackFlow`, so the Compose UI updates on every token with no polling:

```kotlin
session.generateContentStream(
    listOf(InputData.Text(prompt)),
    object : ResponseCallback {
        override fun onNext(response: String) { trySend(response) }
        override fun onDone()                 { close() }
        override fun onError(t: Throwable)    { close(t) }
    }
)
```

---

## Models

### LLM — `gemma-4-E2B-it-litert-lm`

| Property | Value |
|---|---|
| Format | `.litertlm` |
| Size | ~2.58 GB |
| Source | Hugging Face — `litert-community/gemma-4-E2B-it-litert-lm` / Google AI Edge |
| License | Gemma Terms of Use |

The model is **not bundled in the APK**. Users download it once to their device and import it on first launch via the in-app model gate. This keeps the APK small and keeps the model weights under the user's direct control.

### Embedding model — `all-MiniLM-L6-v2`

| Property | Value |
|---|---|
| Format | `.tflite` (quantised INT8) |
| Size | ~22 MB |
| Source | `Nihal2000/all-MiniLM-L6-v2-quant.tflite` on Hugging Face |
| License | Apache 2.0 |

The embedding model ships inside `assets/embedding/` and requires no user action.

---

## Setup

### Prerequisites
- Android Studio Meerkat or later
- Physical Android device — emulators are not recommended for heavy on-device ML workloads
- Device with 4 GB+ RAM and ~3 GB free storage

### Steps

**1. Clone and open**
```bash
git clone https://github.com/umerdilpazir/pocketsage.git
```
Open the project in Android Studio and let Gradle sync complete.

**2. Build and install**
```bash
./gradlew installDebug
```

**3. Download the LLM**

Download `gemma-4-E2B-it-litert-lm.litertlm` (~2.58 GB) from Hugging Face (`litert-community/gemma-4-E2B-it-litert-lm`) to your phone's `Downloads` folder. You will need to accept the Gemma licence.

Alternatively, push it via ADB:
```bash
adb push gemma-4-E2B-it-litert-lm.litertlm /sdcard/Download/
```

**4. Import the model**

Launch PocketSage. The Model Gate screen appears automatically. Tap **Pick gemma2b.litertlm**, navigate to `Downloads`, and select the file. A progress bar tracks the copy into app-private storage. The app advances to the main screen once complete.

**5. Add a PDF and start chatting**

Tap **+** on the Library screen to import a PDF. Once ingestion completes, tap the document and ask a question. Answers stream token by token with cited source snippets shown below each response.

---

## Screenshots

| Library | Model Import | Chat | Sources |
|---|---|---|---|
| ![Library Screen](docs/screenshots/library.png) | ![Model Import](docs/screenshots/model-import.png) | ![Chat](docs/screenshots/chat.png) | ![Sources](docs/screenshots/sources.png) |
| Managing local PDF documents | One-time model provisioning | Streaming LLM responses | Verifying context with cited sources |

---

## Demo

<div align="center">
<img src="docs/demo.gif" width="320" alt="PocketSage full demo" />
</div>

The GIF above demonstrates: launching the app for the first time, importing the `.litertlm` model via the Model Gate, adding a PDF through the Library screen, and watching an answer stream token by token with retrieved source chunks shown alongside the response.

---

## Engineering Highlights

- **Fully offline RAG on Android** — no network permission, no cloud dependency at any stage of the pipeline
- **Local vector retrieval** — cosine similarity over Room-backed BLOB storage; no third-party vector DB required
- **Real-time streaming generation** — `callbackFlow` bridges the native `ResponseCallback` into a Kotlin `Flow<String>` consumed directly by a Compose `StateFlow`
- **Clean architectural separation** — `EmbeddingService` and `LlmRunner` domain interfaces fully decouple the retrieval engine from the generation engine; both are independently testable and swappable
- **Secure model provisioning** — the LLM lives in app-private `filesDir`; the import flow uses the Storage Access Framework with a copy-and-verify pattern and a progress indicator
- **Production-style Android architecture** — Hilt for DI, Room for persistence, `Result<T>` for cross-layer error propagation, sealed `UiState` interfaces, stateless Composables hoisted to ViewModels

---

## Known Constraints

- Requires a modern Android device with at least 4 GB RAM and ~3 GB of free storage for the LLM model file
- First-time `engine.initialize()` can take 5–15 seconds depending on device hardware; a loading indicator covers this
- Output quality depends on the selected local model and the relevance of the retrieved chunks — Gemma 2B produces solid extractive answers but is not a reasoning model
- Cosine retrieval is brute-force over in-memory vectors; suitable for thousands of chunks; an ANN index would be needed at larger scale

---

## Conclusion

PocketSage is a practical demonstration that a complete, production-quality Retrieval-Augmented Generation pipeline can be built entirely on an Android device — no servers, no API keys, no compromises on user privacy. It combines deep Android engineering (Compose, Hilt, Room, Coroutines) with hands-on applied AI work (embedding inference, vector retrieval, streaming LLM generation) in a codebase that is clean, layered, and built to the same standards as production Android applications.

---

<div align="center">

If PocketSage is useful to you, a ⭐ on the repo helps others find it.

</div>