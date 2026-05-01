package com.umer.pocketsage.ui.library

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.umer.pocketsage.domain.Document
import com.umer.pocketsage.domain.IngestProgress
import com.umer.pocketsage.ui.theme.PocketSageTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenChat: (docId: String?) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val ingest by viewModel.ingest.collectAsState()
    val context = LocalContext.current

    var deleteTarget by remember { mutableStateOf<Document?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val title = context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else null
            }
            ?.removeSuffix(".pdf")
            ?: uri.lastPathSegment
            ?: "Document"
        viewModel.onPick(uri, title)
    }

    val showProgress = ingest != null &&
            ingest !is IngestProgress.Done &&
            ingest !is IngestProgress.Error

    Scaffold(
        topBar = { TopAppBar(title = { Text("Library") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePicker.launch(arrayOf("application/pdf")) },
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add PDF")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (showProgress) {
                val fraction = (ingest as? IngestProgress.Embedding)
                    ?.let { it.done.toFloat() / it.total.toFloat() }
                if (fraction != null) {
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            when (val s = state) {
                LibraryUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                LibraryUiState.Empty -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { Text("Add a PDF to get started") }

                is LibraryUiState.Loaded -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(s.docs, key = { it.id }) { doc ->
                        DocumentRow(
                            doc = doc,
                            onClick = { onOpenChat(doc.id) },
                            onLongClick = { deleteTarget = doc },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    deleteTarget?.let { doc ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete \"${doc.title}\"?") },
            text = { Text("This will remove the document and all its chunks.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(doc.id)
                    deleteTarget = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DocumentRow(
    doc: Document,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val date = remember(doc.createdAt) {
        Instant.ofEpochMilli(doc.createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
    ListItem(
        headlineContent = { Text(doc.title) },
        supportingContent = { Text("${doc.chunkCount} chunks · $date") },
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun LibraryEmptyPreview() {
    PocketSageTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Library") }) },
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = "Add PDF")
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { Text("Add a PDF to get started") }
        }
    }
}