package com.umer.pocketsage.data.retrieval

import com.umer.pocketsage.data.Cosine
import org.junit.Assert.assertEquals
import org.junit.Test

class CosineTest {

    @Test
    fun `given identical unit vectors_when similarity computed_then returns 1`() {
        assertEquals(1f, Cosine.similarity(floatArrayOf(1f, 0f, 0f), floatArrayOf(1f, 0f, 0f)), 1e-6f)
    }

    @Test
    fun `given orthogonal vectors_when similarity computed_then returns 0`() {
        assertEquals(0f, Cosine.similarity(floatArrayOf(1f, 0f), floatArrayOf(0f, 1f)), 1e-6f)
    }

    @Test
    fun `given zero vector_when similarity computed_then returns 0`() {
        assertEquals(0f, Cosine.similarity(floatArrayOf(0f, 0f), floatArrayOf(1f, 1f)), 1e-6f)
    }
}