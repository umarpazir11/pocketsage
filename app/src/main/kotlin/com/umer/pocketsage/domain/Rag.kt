package com.umer.pocketsage.domain

import kotlinx.coroutines.flow.Flow

sealed interface ChatScope {
    object AllDocuments : ChatScope
    data class OneDocument(val id: String) : ChatScope
}

sealed interface RagEvent {
    object Retrieving : RagEvent
    data class Retrieved(val sources: List<RetrievedChunk>) : RagEvent
    data class Token(val text: String) : RagEvent
    object Completed : RagEvent
    data class Error(val t: Throwable) : RagEvent
}

interface RagPipeline {
    fun answer(question: String, scope: ChatScope): Flow<RagEvent>
}