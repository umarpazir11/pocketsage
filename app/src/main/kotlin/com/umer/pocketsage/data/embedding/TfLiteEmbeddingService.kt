package com.umer.pocketsage.data.embedding

import com.umer.pocketsage.domain.EmbeddingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TfLiteEmbeddingService @Inject constructor(
    private val assets: AssetLoader,
) : EmbeddingService {

    private val interpreter: Interpreter by lazy {
        Interpreter(assets.loadModel(), Interpreter.Options().apply {
            numThreads = 4
            useNNAPI = false // NNAPI can be unstable on some chipsets; validate per-device before enabling
        })
    }

    private val tokenizer: BertTokenizer by lazy { BertTokenizer(assets.loadVocab()) }

    // Interpreter is not concurrent-call-safe; Mutex serialises access across coroutines.
    private val mutex = Mutex()

    override suspend fun embed(text: String): FloatArray = withContext(Dispatchers.Default) {
        val (ids, mask, typeIds) = tokenizer.encode(text)
        mutex.withLock {
            val inCount = interpreter.inputTensorCount
            val inputs: Array<Any> = if (inCount == 2) {
                arrayOf(Array(1) { ids }, Array(1) { mask })
            } else {
                arrayOf(Array(1) { ids }, Array(1) { mask }, Array(1) { typeIds })
            }

            val outShape = interpreter.getOutputTensor(0).shape()
            when (outShape.size) {
                3 -> {
                    val out = Array(1) { Array(outShape[1]) { FloatArray(outShape[2]) } }
                    interpreter.runForMultipleInputsOutputs(inputs, hashMapOf<Int, Any>(0 to out))
                    meanPool(out[0], mask).also { l2Normalize(it) }
                }
                2 -> {
                    val out = Array(1) { FloatArray(outShape[1]) }
                    interpreter.runForMultipleInputsOutputs(inputs, hashMapOf<Int, Any>(0 to out))
                    out[0].also { l2Normalize(it) }
                }
                else -> error("unexpected output rank ${outShape.size}")
            }
        }
    }
}