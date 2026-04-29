package com.umer.pocketsage.domain

interface EmbeddingService {
    suspend fun embed(text: String): FloatArray
}