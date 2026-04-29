package com.umer.pocketsage.data.embedding

import com.umer.pocketsage.domain.EmbeddingService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

/**
 * JVM-only pipeline test: verifies cosine similarity properties without loading TFLite.
 *
 * Uses a [FakeEmbeddingService] that converts character codes into a float vector and
 * L2-normalises it — enough to satisfy identical-string and dissimilar-string properties
 * without any Android or native dependency.
 */
class EmbeddingPipelineTest {

    private val service: EmbeddingService = FakeEmbeddingService()

    @Test
    fun `cosine similarity of any two embeddings is between -1 and 1`() = runBlocking {
        val a = service.embed("a cat sat on the mat")
        val b = service.embed("quantum chromodynamics")
        val sim = cosine(a, b)
        assertTrue("cosine $sim < -1", sim >= -1f)
        assertTrue("cosine $sim > 1", sim <= 1f)
    }

    @Test
    fun `string is more similar to itself than to an unrelated string`() = runBlocking {
        val text = "a cat sat on the mat"
        val selfSim  = cosine(service.embed(text), service.embed(text))
        val crossSim = cosine(service.embed(text), service.embed("quantum chromodynamics"))
        assertTrue(
            "selfSim ($selfSim) should be greater than crossSim ($crossSim)",
            selfSim > crossSim,
        )
    }

    // ---------------------------------------------------------------------------

    private fun cosine(a: FloatArray, b: FloatArray): Float {
        var dot = 0f; var na = 0f; var nb = 0f
        for (i in a.indices) { dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i] }
        return if (na == 0f || nb == 0f) 0f else dot / (sqrt(na) * sqrt(nb))
    }

    /**
     * Deterministic embedding that needs no vocab or native libs.
     * Scatters character codepoints into a 128-bucket float array and L2-normalises.
     * An identical string always yields cosine = 1.0; a different string yields < 1.0.
     */
    private class FakeEmbeddingService : EmbeddingService {
        override suspend fun embed(text: String): FloatArray {
            val v = FloatArray(128)
            text.forEachIndexed { i, c -> v[i % 128] += c.code.toFloat() }
            l2Normalize(v)
            return v
        }
    }
}