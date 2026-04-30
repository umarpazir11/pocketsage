package com.umer.pocketsage.domain

import kotlinx.coroutines.flow.Flow

interface LlmRunner {
    /** Emits partial text tokens as they are generated. Completes when generation is done. */
    fun generate(prompt: String): Flow<String>
}