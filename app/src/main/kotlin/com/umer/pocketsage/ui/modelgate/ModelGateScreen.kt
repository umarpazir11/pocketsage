package com.umer.pocketsage.ui.modelgate

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ModelGate(
    viewModel: ModelGateViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState is ModelGateUiState.Ready) {
        content()
        return
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFromUri(it) }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            when (val state = uiState) {
                ModelGateUiState.Idle -> {
                    Text(
                        text = "Gemma 2B model required",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Download gemma2b.litertlm from ai.google.dev/edge/litert-lm/android " +
                            "and pick the file below. See the README for step-by-step instructions.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = {
                        launcher.launch(arrayOf("application/octet-stream", "*/*"))
                    }) {
                        Text("Pick gemma2b.litertlm")
                    }
                }

                is ModelGateUiState.Importing -> {
                    Text(
                        text = "Copying model…",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${(state.progress * 100).toInt()} %",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                is ModelGateUiState.Error -> {
                    Text(
                        text = "Import failed",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = {
                        launcher.launch(arrayOf("application/octet-stream", "*/*"))
                    }) {
                        Text("Try again")
                    }
                }

                ModelGateUiState.Ready -> Unit
            }
        }
    }
}