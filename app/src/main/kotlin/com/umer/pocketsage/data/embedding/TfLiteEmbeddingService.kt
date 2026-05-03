package com.umer.pocketsage.data.embedding

import com.umer.pocketsage.domain.EmbeddingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class TfLiteEmbeddingService @Inject constructor(
    private val assetLoader: AssetLoader
) : EmbeddingService {

    private val vocab: Map<String, Int> by lazy { assetLoader.loadVocab() }

    private val interpreter: Interpreter by lazy {
        Interpreter(assetLoader.loadModel(), Interpreter.Options().setNumThreads(4))
    }

    override suspend fun embed(text: String): FloatArray = withContext(Dispatchers.Default) {
        val inputIds = tokenize(text)
        val attentionMask = maskFor(inputIds)
        val output = Array(1) { FloatArray(EMBEDDING_DIM) }

        interpreter.runForMultipleInputsOutputs(
            arrayOf(inputIds, attentionMask),
            mapOf(0 to output)
        )
        l2Normalize(output[0])
    }

    private fun tokenize(text: String): Array<IntArray> {
        val clsId = vocab["[CLS]"] ?: 101
        val sepId = vocab["[SEP]"] ?: 102
        val unkId = vocab["[UNK]"] ?: 100

        val tokens = mutableListOf(clsId)
        text.lowercase().split(Regex("[\\s\\p{Punct}]+")).forEach { word ->
            if (word.isNotEmpty()) tokens.add(vocab[word] ?: unkId)
        }
        tokens.add(sepId)

        while (tokens.size < MAX_SEQ_LEN) tokens.add(0)
        return arrayOf(tokens.take(MAX_SEQ_LEN).toIntArray())
    }

    private fun maskFor(inputIds: Array<IntArray>): Array<IntArray> =
        arrayOf(IntArray(inputIds[0].size) { if (inputIds[0][it] != 0) 1 else 0 })

    private fun l2Normalize(vec: FloatArray): FloatArray {
        val norm = sqrt(vec.fold(0f) { acc, v -> acc + v * v })
        return FloatArray(vec.size) { vec[it] / norm.coerceAtLeast(1e-9f) }
    }

    companion object {
        private const val MAX_SEQ_LEN = 128
        private const val EMBEDDING_DIM = 384
    }
}