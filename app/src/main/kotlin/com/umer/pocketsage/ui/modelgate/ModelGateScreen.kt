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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umer.pocketsage.R

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
                        text = stringResource(R.string.model_required_title),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    val bodyText = stringResource(R.string.model_required_body)
                    val linkLabel = stringResource(R.string.model_download_link_label)
                    val url = stringResource(R.string.model_download_url)
                    val linkColor = MaterialTheme.colorScheme.primary
                    Text(
                        text = buildAnnotatedString {
                            val linkStart = bodyText.indexOf(linkLabel)
                            append(bodyText.substring(0, linkStart))
                            withLink(LinkAnnotation.Url(url)) {
                                withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                                    append(linkLabel)
                                }
                            }
                            append(bodyText.substring(linkStart + linkLabel.length))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    val uriHandler = LocalUriHandler.current
                    val directDownloadUrl = stringResource(R.string.model_direct_download_url)
                    OutlinedButton(onClick = { uriHandler.openUri(directDownloadUrl) }) {
                        Text(stringResource(R.string.download_model_button))
                    }
                    Button(onClick = {
                        launcher.launch(arrayOf("application/octet-stream", "*/*"))
                    }) {
                        Text(stringResource(R.string.import_model_button))
                    }
                }

                is ModelGateUiState.Importing -> {
                    Text(
                        text = stringResource(R.string.model_copying),
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
                        text = stringResource(R.string.model_import_failed),
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
                        Text(stringResource(R.string.try_again))
                    }
                }

                ModelGateUiState.Ready -> Unit
            }
        }
    }
}