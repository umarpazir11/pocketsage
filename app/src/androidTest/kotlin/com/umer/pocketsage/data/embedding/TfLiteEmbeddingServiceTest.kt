package com.umer.pocketsage.data.embedding

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.sqrt

@RunWith(AndroidJUnit4::class)
class TfLiteEmbeddingServiceTest {

    private lateinit var service: TfLiteEmbeddingService

    @Before
    fun setUp() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val loader = AssetLoader(ctx, "embedding/all-MiniLM-L6-v2.tflite", "embedding/vocab.txt")
        service = TfLiteEmbeddingService(loader)
    }

    @Test
    fun identicalSentencesHaveCosineSimilarityNearOne() = runBlocking {
        val a = service.embed("a cat sat on the mat")
        val b = service.embed("a cat sat on the mat")
        val sim = cosine(a, b)
        assertTrue("Expected cosine ≈ 1.0, got $sim", sim > 0.99f)
    }

    @Test
    fun dissimilarSentencesHaveLowCosineSimilarity() = runBlocking {
        val a = service.embed("a cat sat on the mat")
        val b = service.embed("quantum chromodynamics")
        val sim = cosine(a, b)
        assertTrue("Expected cosine < 0.6, got $sim", sim < 0.6f)
    }

    private fun cosine(a: FloatArray, b: FloatArray): Float {
        var dot = 0f; var na = 0f; var nb = 0f
        for (i in a.indices) { dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i] }
        return if (na == 0f || nb == 0f) 0f else dot / (sqrt(na) * sqrt(nb))
    }
}