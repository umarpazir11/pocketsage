package com.umer.pocketsage.domain

data class RetrievedChunk(
    val text: String,
    val documentId: String,
    val score: Float,
)

interface Retriever {
    suspend fun topK(
        query: FloatArray,
        documentIds: List<String>? = null,
        k: Int = 4,
    ): List<RetrievedChunk>
}
