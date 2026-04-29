package com.umer.pocketsage.ui.modelgate

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umer.pocketsage.data.llm.ModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ModelGateUiState {
    data object Idle : ModelGateUiState
    data class Importing(val progress: Float) : ModelGateUiState
    data object Ready : ModelGateUiState
    data class Error(val message: String) : ModelGateUiState
}

@HiltViewModel
class ModelGateViewModel @Inject constructor(
    private val modelRepository: ModelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ModelGateUiState>(
        if (modelRepository.isModelReady()) ModelGateUiState.Ready else ModelGateUiState.Idle
    )
    val uiState: StateFlow<ModelGateUiState> = _uiState.asStateFlow()

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ModelGateUiState.Importing(0f)

            val progressJob = launch {
                modelRepository.importProgress.collect { progress ->
                    if (_uiState.value is ModelGateUiState.Importing) {
                        _uiState.value = ModelGateUiState.Importing(progress)
                    }
                }
            }

            val result = modelRepository.importFromUri(uri)
            progressJob.cancel()

            result
                .onSuccess { _uiState.value = ModelGateUiState.Ready }
                .onFailure { _uiState.value = ModelGateUiState.Error(it.message ?: "Import failed") }
        }
    }
}