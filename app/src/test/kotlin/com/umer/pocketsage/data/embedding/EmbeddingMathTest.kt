package com.umer.pocketsage.data.embedding

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sqrt

class EmbeddingMathTest {

    // --- meanPool ---

    @Test
    fun `meanPool averages non-padding tokens from 1x3x4 tensor`() {
        // shape [1,3,4] unrolled; mask selects first 2 tokens
        val embeddings = arrayOf(
            floatArrayOf(1f, 2f, 3f, 4f),
            floatArrayOf(3f, 4f, 5f, 6f),
            floatArrayOf(100f, 100f, 100f, 100f), // padding — must not contribute
        )
        val result = meanPool(embeddings, intArrayOf(1, 1, 0))
        assertArrayEquals(floatArrayOf(2f, 3f, 4f, 5f), result, 1e-5f)
    }

    @Test
    fun `meanPool with all tokens active averages all rows`() {
        val embeddings = arrayOf(
            floatArrayOf(0f, 0f, 4f),
            floatArrayOf(0f, 0f, 8f),
        )
        val result = meanPool(embeddings, intArrayOf(1, 1))
        assertArrayEquals(floatArrayOf(0f, 0f, 6f), result, 1e-5f)
    }

    @Test
    fun `meanPool with single active token returns that row`() {
        val embeddings = arrayOf(
            floatArrayOf(7f, 8f, 9f),
            floatArrayOf(0f, 0f, 0f),
        )
        val result = meanPool(embeddings, intArrayOf(1, 0))
        assertArrayEquals(floatArrayOf(7f, 8f, 9f), result, 1e-5f)
    }

    // --- l2Normalize ---

    @Test
    fun `l2Normalize scales vector to unit length`() {
        val v = floatArrayOf(3f, 4f, 0f, 0f) // Euclidean norm = 5
        l2Normalize(v)
        assertArrayEquals(floatArrayOf(0.6f, 0.8f, 0f, 0f), v, 1e-5f)
    }

    @Test
    fun `l2Normalize result has norm 1`() {
        val v = floatArrayOf(1f, 2f, 3f, 4f)
        l2Normalize(v)
        val norm = sqrt(v.fold(0f) { acc, x -> acc + x * x })
        assertEquals(1f, norm, 1e-5f)
    }

    @Test
    fun `l2Normalize zero vector is left unchanged`() {
        val v = floatArrayOf(0f, 0f, 0f)
        l2Normalize(v) // must not throw or produce NaN
        assertArrayEquals(floatArrayOf(0f, 0f, 0f), v, 1e-5f)
    }
}