package com.umer.pocketsage.data.repository

import com.umer.pocketsage.data.Cosine
import com.umer.pocketsage.data.db.ChunkDao
import com.umer.pocketsage.di.DefaultDispatcher
import com.umer.pocketsage.domain.RetrievedChunk
import com.umer.pocketsage.domain.Retriever
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrieverImpl @Inject constructor(
    private val chunkDao: ChunkDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : Retriever {

    override suspend fun topK(
        query: FloatArray,
        documentIds: List<String>?,
        k: Int,
    ): List<RetrievedChunk> = withContext(dispatcher) {
        // TODO: replace with ANN if chunk count grows beyond a few thousand
        val entities = if (documentIds != null) chunkDao.getForDocuments(documentIds) else chunkDao.getAll()
        entities
            .map { entity ->
                RetrievedChunk(
                    text = entity.text,
                    documentId = entity.documentId,
                    score = Cosine.similarity(query, entity.embedding.toFloatArray()),
                )
            }
            .sortedByDescending { it.score }
            .take(k)
    }

    private fun ByteArray.toFloatArray(): FloatArray {
        val buf = ByteBuffer.wrap(this).asFloatBuffer()
        return FloatArray(buf.remaining()) { buf.get() }
    }
}