package com.umer.pocketsage.data.db

import com.umer.pocketsage.data.embedding.toByteArray
import com.umer.pocketsage.data.embedding.toFloatArray
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class VecTest {

    @Test
    fun `FloatArray round-trips through ByteArray with exact bit equality`() {
        val original = floatArrayOf(1.5f, -2.3f, 0f, Float.MAX_VALUE, Float.MIN_VALUE, Float.NaN)
        assertArrayEquals(original, original.toByteArray().toFloatArray(), 0f)
    }

    @Test
    fun `ByteArray size is 4 x FloatArray size`() {
        val floats = FloatArray(384) { it.toFloat() }
        assertEquals(384 * Float.SIZE_BYTES, floats.toByteArray().size)
    }

    @Test
    fun `empty FloatArray round-trips to empty ByteArray and back`() {
        val bytes = floatArrayOf().toByteArray()
        assertEquals(0, bytes.size)
        assertEquals(0, bytes.toFloatArray().size)
    }

    @Test
    fun `negative and fractional values are preserved exactly`() {
        val original = floatArrayOf(-1f, 0.1f, -0.1f, 1e-10f, -1e10f)
        assertArrayEquals(original, original.toByteArray().toFloatArray(), 0f)
    }
}