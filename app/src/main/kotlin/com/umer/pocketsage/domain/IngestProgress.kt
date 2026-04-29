package com.umer.pocketsage.domain

sealed interface IngestProgress {
    data object Started : IngestProgress
    data object Extracting : IngestProgress
    data class Chunking(val count: Int) : IngestProgress
    data class Embedding(val done: Int, val total: Int) : IngestProgress
    data class Done(val documentId: String) : IngestProgress
    data class Error(val throwable: Throwable) : IngestProgress
}
