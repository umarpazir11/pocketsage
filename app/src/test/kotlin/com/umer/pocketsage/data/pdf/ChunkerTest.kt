package com.umer.pocketsage.data.pdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChunkerTest {

    private val chunker = Chunker()

    @Test
    fun `empty string returns empty list`() {
        assertTrue(chunker.chunk("").isEmpty())
    }

    @Test
    fun `blank string returns empty list`() {
        assertTrue(chunker.chunk("   \n  ").isEmpty())
    }

    @Test
    fun `text shorter than size returns single chunk`() {
        val text = "hello world, this is a short document"
        val result = chunker.chunk(text, size = 800, overlap = 120)
        assertEquals(1, result.size)
        assertEquals(text, result[0])
    }

    @Test
    fun `text of 2000 chars produces 3 chunks with correct overlap`() {
        // Use a no-whitespace string so chunk boundaries are exact (no word-boundary adjustments)
        val text = (0 until 2000).map { ('a' + it % 26) }.joinToString("")
        val result = chunker.chunk(text, size = 800, overlap = 120)

        assertEquals(3, result.size)

        // step = 800 - 120 = 680; starts: 0, 680, 1360
        assertEquals(800, result[0].length)
        assertEquals(800, result[1].length)
        // chunk 2 covers text[1360..2000) = 640 chars
        assertEquals(640, result[2].length)

        // Consecutive chunks share exactly 120 chars
        assertEquals(result[0].takeLast(120), result[1].take(120))
        assertEquals(result[1].takeLast(120), result[2].take(120))
    }

    @Test
    fun `chunk respects word boundaries`() {
        // Place a space just before the cut point so the chunker ends the chunk there
        val before = "a".repeat(795) + "word "  // 801 chars, space at position 800
        val after = "b".repeat(800)
        val text = before + after  // 1601 chars
        val result = chunker.chunk(text, size = 800, overlap = 120)

        // The first chunk should not end mid-word; it should end at the space boundary
        assertTrue("Chunk should not end with a word fragment", !result[0].endsWith("wor"))
    }
}