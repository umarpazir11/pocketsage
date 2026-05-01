package com.umer.pocketsage.ui.chat

import com.umer.pocketsage.domain.RetrievedChunk

enum class Role { User, Assistant }

data class ChatMessage(
    val id: Long,
    val role: Role,
    val text: String,
    val sources: List<RetrievedChunk> = emptyList(),
)