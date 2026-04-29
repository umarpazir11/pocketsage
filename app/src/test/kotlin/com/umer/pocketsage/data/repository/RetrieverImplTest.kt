package com.umer.pocketsage.data.repository

import com.umer.pocketsage.data.db.ChunkDao
import com.umer.pocketsage.data.db.ChunkEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer

@OptIn(ExperimentalCoroutinesApi::class)
class RetrieverImplTest {

    private val fakeDao = FakeChunkDao()
    private val retriever = RetrieverImpl(fakeDao, UnconfinedTestDispatcher())

    @Test
    fun `given 5 chunks_when topK 2 called_then returns 2 highest scoring chunks`() = runTest {
        fakeDao.chunks = listOf(
            chunk("doc1", "low-a",    floatArrayOf(0f, 1f)),  // score ~ 0
            chunk("doc1", "high-a",   floatArrayOf(1f, 0f)),  // score = 1
            chunk("doc2", "mid",      floatArrayOf(0.7f, 0.7f)), // score ~ 0.707
            chunk("doc2", "low-b",    floatArrayOf(0f, 1f)),  // score ~ 0
            chunk("doc2", "high-b",   floatArrayOf(1f, 0f)),  // score = 1
        )
        val query = floatArrayOf(1f, 0f)

        val results = retriever.topK(query, k = 2)

        assertEquals(2, results.size)
        results.forEach { assertEquals(1f, it.score, 1e-6f) }
        assertEquals(setOf("high-a", "high-b"), results.map { it.text }.toSet())
    }

    @Test
    fun `given doc filter_when topK called_then only returns chunks from those docs`() = runTest {
        fakeDao.chunks = listOf(
            chunk("doc1", "d1-chunk", floatArrayOf(1f, 0f)),
            chunk("doc2", "d2-chunk", floatArrayOf(1f, 0f)),
        )
        val query = floatArrayOf(1f, 0f)

        val results = retriever.topK(query, documentIds = listOf("doc1"), k = 4)

        assertEquals(1, results.size)
        assertEquals("doc1", results[0].documentId)
    }

    // --- helpers ---

    private fun chunk(docId: String, text: String, vec: FloatArray) = ChunkEntity(
        documentId = docId,
        ordinal = 0,
        text = text,
        embedding = vec.toByteArray(),
    )

    private fun FloatArray.toByteArray(): ByteArray =
        ByteBuffer.allocate(size * 4).also { it.asFloatBuffer().put(this) }.array()
}

private class FakeChunkDao : ChunkDao {
    var chunks: List<ChunkEntity> = emptyList()

    override suspend fun insertAll(chunks: List<ChunkEntity>) { this.chunks = chunks }
    override suspend fun getForDocuments(ids: List<String>) = chunks.filter { it.documentId in ids }
    override suspend fun getAll() = chunks
    override suspend fun deleteForDocument(id: String) { chunks = chunks.filter { it.documentId != id } }
}