package com.umer.pocketsage.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umer.pocketsage.domain.ChatScope
import com.umer.pocketsage.domain.DocumentRepository
import com.umer.pocketsage.domain.RagEvent
import com.umer.pocketsage.domain.RagPipeline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rag: RagPipeline,
    repo: DocumentRepository,
) : ViewModel() {

    private val docId: String? = savedStateHandle["docId"]
    private val chatScope: ChatScope = if (docId == null) ChatScope.AllDocuments
    else ChatScope.OneDocument(docId)

    private val allDocs = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val docTitle: StateFlow<String> = allDocs
        .map { docs ->
            if (docId == null) "All Documents"
            else docs.find { it.id == docId }?.title ?: "Document"
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            if (docId == null) "All Documents" else "Document",
        )

    val docTitles: StateFlow<Map<String, String>> = allDocs
        .map { docs -> docs.associate { it.id to it.title } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val nextId = AtomicLong(0)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private var ragJob: Job? = null

    fun send(text: String) {
        if (text.isBlank()) return
        ragJob?.cancel()

        val userMsg = ChatMessage(id = nextId.getAndIncrement(), role = Role.User, text = text)
        val assistantMsg = ChatMessage(id = nextId.getAndIncrement(), role = Role.Assistant, text = "")
        _messages.update { it + userMsg + assistantMsg }

        ragJob = viewModelScope.launch {
            rag.answer(text, chatScope).collect { event ->
                when (event) {
                    is RagEvent.Retrieving -> _isGenerating.value = true
                    is RagEvent.Retrieved -> updateLastAssistant { it.copy(sources = event.sources) }
                    is RagEvent.Token -> updateLastAssistant { it.copy(text = it.text + event.text) }
                    is RagEvent.Completed, is RagEvent.Error -> _isGenerating.value = false
                }
            }
        }
    }

    private fun updateLastAssistant(transform: (ChatMessage) -> ChatMessage) {
        _messages.update { msgs ->
            val idx = msgs.indexOfLast { it.role == Role.Assistant }
            if (idx < 0) return@update msgs
            msgs.toMutableList().also { it[idx] = transform(it[idx]) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ragJob?.cancel()
    }
}