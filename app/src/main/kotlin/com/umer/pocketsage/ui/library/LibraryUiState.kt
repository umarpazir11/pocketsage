package com.umer.pocketsage.ui.library

import com.umer.pocketsage.domain.Document

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data object Empty : LibraryUiState
    data class Loaded(val docs: List<Document>) : LibraryUiState
}