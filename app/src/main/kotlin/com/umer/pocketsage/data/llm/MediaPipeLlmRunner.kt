package com.umer.pocketsage.data.llm

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.umer.pocketsage.domain.LlmRunner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPipeLlmRunner @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val modelRepo: ModelRepository,
) : LlmRunner {

    // Routes result-listener callbacks to whichever Flow is currently collecting.
    private val activeChannel = AtomicReference<SendChannel<String>?>(null)

    // Lazy so the ~5-10 s model load only happens on first generate() call.
    // Always first accessed via withContext(IO) to keep it off the main thread.
    private val inference: LlmInference by lazy {
        LlmInference.createFromOptions(
            ctx,
            LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelRepo.getModelPath().absolutePath)
                .setMaxTokens(1024)
                .setTopK(40)
                .setTemperature(0.7f)
                .setRandomSeed(0)
                .setResultListener { partial, done ->
                    val ch = activeChannel.get() ?: return@setResultListener
                    ch.trySend(partial)
                    if (done) {
                        ch.close()
                        activeChannel.compareAndSet(ch, null)
                    }
                }
                .setErrorListener { e ->
                    activeChannel.getAndSet(null)
                        ?.close(RuntimeException(e?.message ?: "LlmInference error"))
                }
                .build()
        )
    }

    override fun generate(prompt: String): Flow<String> = callbackFlow {
        // Close any in-flight flow so its collector stops receiving tokens.
        activeChannel.getAndSet(channel)
            ?.close(CancellationException("Superseded by new generate()"))

        // Ensure the lazy model load happens on IO, not the caller's thread.
        withContext(Dispatchers.IO) { inference }
            .generateResponseAsync(prompt)

        awaitClose {
            activeChannel.compareAndSet(channel, null)
        }
    }
}