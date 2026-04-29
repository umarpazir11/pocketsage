package com.umer.pocketsage.domain

import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeAll(): Flow<List<Document>>
    suspend fun delete(id: String)
    fun ingest(uriString: String, title: String): Flow<IngestProgress>
}
