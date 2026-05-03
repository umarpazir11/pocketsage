package com.umer.pocketsage.data.llm

import android.content.Context
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.InputData
import com.google.ai.edge.litertlm.ResponseCallback
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.Session
import com.google.ai.edge.litertlm.SessionConfig
import com.umer.pocketsage.domain.LlmRunner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiteRtLmRunner @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val modelRepo: ModelRepository,
) : LlmRunner {

    private val engine: Engine by lazy {
        Engine(
            EngineConfig(
                modelPath = modelRepo.getModelPath().absolutePath,
                cacheDir = ctx.cacheDir.absolutePath,
            )
        ).also { it.initialize() }
    }

    private val sessionConfig = SessionConfig(
        SamplerConfig(topK = 40, topP = 0.9, temperature = 0.7, seed = 0)
    )

    private var activeSession: Session? = null

    override fun generate(prompt: String): Flow<String> = callbackFlow {
        check(modelRepo.isModelReady()) {
            "Model not ready — engine init would crash"
        }
        activeSession?.cancelProcess()
        activeSession?.close()

        val session = engine.createSession(sessionConfig)
        activeSession = session

        session.generateContentStream(
            listOf(InputData.Text(prompt)),
            object : ResponseCallback {
                override fun onNext(response: String) {
                    trySend(response)
                }
                override fun onDone() {
                    close()
                }
                override fun onError(throwable: Throwable) {
                    close(throwable)
                }
            }
        )

        awaitClose {
            activeSession = null
            session.close()
        }
    }.flowOn(Dispatchers.IO)

    fun close() {
        activeSession?.close()
        engine.close()
    }
}