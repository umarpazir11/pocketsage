package com.umer.pocketsage.ui.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umer.pocketsage.R
import com.umer.pocketsage.domain.RetrievedChunk
import com.umer.pocketsage.ui.theme.PocketSageTheme

private val UserBubbleShape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
private val AssistantBubbleShape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)

@Composable
fun ChatScreen(
    onNavigateUp: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val docTitle by viewModel.docTitle.collectAsState()
    val docTitles by viewModel.docTitles.collectAsState()

    ChatContent(
        messages = messages,
        isGenerating = isGenerating,
        docTitle = docTitle,
        docTitles = docTitles,
        onSend = viewModel::send,
        onNavigateUp = onNavigateUp,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatContent(
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    docTitle: String,
    docTitles: Map<String, String>,
    onSend: (String) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val lastAssistantId = messages.lastOrNull { it.role == Role.Assistant }?.id

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(docTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            LazyColumn(
                reverseLayout = true,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(messages.reversed(), key = { it.id }) { msg ->
                    MessageBubble(
                        msg = msg,
                        docTitles = docTitles,
                        showTyping = isGenerating && msg.id == lastAssistantId,
                    )
                }
            }

            HorizontalDivider()
            InputRow(isGenerating = isGenerating, onSend = onSend)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    msg: ChatMessage,
    docTitles: Map<String, String>,
    showTyping: Boolean,
    modifier: Modifier = Modifier,
) {
    val isUser = msg.role == Role.User
    var showSources by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        ) {
            Surface(
                shape = if (isUser) UserBubbleShape else AssistantBubbleShape,
                color = if (isUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = msg.text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (showTyping) {
                TypingIndicator(modifier = Modifier.padding(start = 4.dp, top = 4.dp))
            }

            if (msg.sources.isNotEmpty()) {
                AssistChip(
                    onClick = { showSources = !showSources },
                    label = { Text(stringResource(R.string.sources_count, msg.sources.size)) },
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (showSources) {
                    Card(modifier = Modifier.padding(top = 4.dp)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            msg.sources.forEach { chunk ->
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = docTitles[chunk.documentId] ?: chunk.documentId,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    val preview = if (chunk.text.length > 200)
                                        chunk.text.take(200) + "…" else chunk.text
                                    Text(
                                        text = preview,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "typing-alpha",
    )
    Text(
        text = "● ● ●",
        modifier = modifier.alpha(alpha),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun InputRow(
    isGenerating: Boolean,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val canSend = text.isNotBlank() && !isGenerating

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(stringResource(R.string.chat_input_hint)) },
            modifier = Modifier.weight(1f),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (canSend) {
                    onSend(text)
                    text = ""
                }
            }),
            shape = RoundedCornerShape(24.dp),
        )
        IconButton(
            onClick = {
                if (canSend) {
                    onSend(text)
                    text = ""
                }
            },
            enabled = canSend,
            modifier = Modifier.padding(start = 4.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.send_button),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, heightDp = 600)
@Composable
private fun ChatPreview() {
    PocketSageTheme {
        ChatContent(
            messages = listOf(
                ChatMessage(
                    id = 0L,
                    role = Role.User,
                    text = "What is machine learning?",
                ),
                ChatMessage(
                    id = 1L,
                    role = Role.Assistant,
                    text = "Machine learning is a subset of AI that enables systems to learn and improve from experience without being explicitly programmed.",
                    sources = listOf(
                        RetrievedChunk(
                            text = "Machine learning (ML) is a field of inquiry devoted to understanding and building methods that learn, improve performance by accessing data.",
                            documentId = "doc1",
                            score = 0.95f,
                        ),
                    ),
                ),
            ),
            isGenerating = false,
            docTitle = "ML Textbook",
            docTitles = mapOf("doc1" to "ML Textbook"),
            onSend = {},
            onNavigateUp = {},
        )
    }
}