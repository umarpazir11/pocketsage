package com.umer.pocketsage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HelloViewModel @Inject constructor() : ViewModel() {
    private val _message = MutableStateFlow("Hilt OK")
    val message: StateFlow<String> = _message.asStateFlow()
}