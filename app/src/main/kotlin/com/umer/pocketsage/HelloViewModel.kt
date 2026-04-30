package com.umer.pocketsage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umer.pocketsage.domain.LlmRunner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelloViewModel @Inject constructor(
    private val llmRunner: LlmRunner,
) : ViewModel() {

    private val _message = MutableStateFlow("Hilt OK")
    val message: StateFlow<String> = _message.asStateFlow()

    fun smokeTest() {
        viewModelScope.launch {
            Log.d(TAG, "smoke › sending prompt: \"Say hi\"")
            var tokenCount = 0
            llmRunner.generate("Say hi").collect { token ->
                tokenCount++
                Log.d(TAG, "token[$tokenCount]: $token")
            }
            Log.d(TAG, "smoke › done ($tokenCount tokens)")
        }
    }

    private companion object {
        const val TAG = "LlmSmoke"
    }
}