package com.umer.pocketsage.ui.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umer.pocketsage.domain.DocumentRepository
import com.umer.pocketsage.domain.IngestProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: DocumentRepository,
) : ViewModel() {

    val state: StateFlow<LibraryUiState> = repo.observeAll()
        .map { docs ->
            if (docs.isEmpty()) LibraryUiState.Empty
            else LibraryUiState.Loaded(docs)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState.Loading,
        )

    private val _ingest = MutableStateFlow<IngestProgress?>(null)
    val ingest: StateFlow<IngestProgress?> = _ingest.asStateFlow()

    fun onPick(uri: Uri, title: String) {
        viewModelScope.launch {
            repo.ingest(uri.toString(), title).collect { _ingest.value = it }
            _ingest.value = null
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }
}