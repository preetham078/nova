package com.preethamm.nova.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.preethamm.nova.conversation.NovaUiState
import com.preethamm.nova.domain.model.AssistantState
import com.preethamm.nova.domain.model.ConversationMessage
import com.preethamm.nova.domain.model.MessageRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaApp(
    state: NovaUiState,
    onMicPressed: () -> Unit,
    onStopPressed: () -> Unit,
    onTypedTextChanged: (String) -> Unit,
    onSendText: () -> Unit,
    onClearConversation: () -> Unit,
    onApiKeyDraftChanged: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onClearApiKey: () -> Unit,
    onRequestMicPermission: () -> Unit
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: NovaDestination.Home.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { NovaBrand(state.demoMode) },
                actions = {
                    IconButton(onClick = { navController.navigate(NovaDestination.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NovaDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                launchSingleTop = true
                                popUpTo(NovaDestination.Home.route)
                            }
                        },
                        icon = {
                            Icon(destination.icon, contentDescription = destination.label)
                        },
                        label = {
                            Text(destination.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NovaDestination.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(NovaDestination.Home.route) {
                HomeScreen(
                    state = state,
                    onMicPressed = onMicPressed,
                    onStopPressed = onStopPressed,
                    onTypedTextChanged = onTypedTextChanged,
                    onSendText = onSendText,
                    onRequestMicPermission = onRequestMicPermission
                )
            }
            composable(NovaDestination.Accounts.route) {
                ConnectedAccountsScreen(state = state, onRequestMicPermission = onRequestMicPermission)
            }
            composable(NovaDestination.Settings.route) {
                SettingsScreen(
                    state = state,
                    onClearConversation = onClearConversation,
                    onApiKeyDraftChanged = onApiKeyDraftChanged,
                    onSaveApiKey = onSaveApiKey,
                    onClearApiKey = onClearApiKey
                )
            }
        }
    }
}

@Composable
private fun NovaBrand(demoMode: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("N", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text("Nova", fontWeight = FontWeight.Bold)
            if (demoMode) {
                Text(
                    "DEMO MODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    state: NovaUiState,
    onMicPressed: () -> Unit,
    onStopPressed: () -> Unit,
    onTypedTextChanged: (String) -> Unit,
    onSendText: () -> Unit,
    onRequestMicPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        StatusPanel(state)
        ConversationList(
            messages = state.messages,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        AnimatedVisibility(visible = state.errorMessage != null) {
            Text(
                text = state.errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        InputDock(
            state = state,
            onMicPressed = onMicPressed,
            onStopPressed = onStopPressed,
            onTypedTextChanged = onTypedTextChanged,
            onSendText = onSendText,
            onRequestMicPermission = onRequestMicPermission
        )
    }
}

@Composable
private fun StatusPanel(state: NovaUiState) {
    val transition = rememberInfiniteTransition(label = "nova-pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "status-pulse"
    )
    val active = state.assistantState == AssistantState.LISTENING ||
        state.assistantState == AssistantState.SPEAKING ||
        state.assistantState == AssistantState.THINKING

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 12.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    state.statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when (state.assistantState) {
                        AssistantState.IDLE -> "Ready when you are."
                        AssistantState.LISTENING -> "Listening for your voice."
                        AssistantState.THINKING -> "Thinking through the request."
                        AssistantState.SPEAKING -> "Speaking the response."
                        AssistantState.ERROR -> "Needs your attention."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(if (active) pulse else 1f)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun ConversationList(
    messages: List<ConversationMessage>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        reverseLayout = true,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(messages.reversed(), key = { it.id }) { message ->
            MessageBubble(message)
        }
    }
}

@Composable
private fun MessageBubble(message: ConversationMessage) {
    val isUser = message.role == MessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.86f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    if (isUser) "You" else "Nova",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(Modifier.height(4.dp))
                Text(message.content, style = MaterialTheme.typography.bodyLarge)
                if (message.isDemoData) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "DEMO DATA",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUser) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InputDock(
    state: NovaUiState,
    onMicPressed: () -> Unit,
    onStopPressed: () -> Unit,
    onTypedTextChanged: (String) -> Unit,
    onSendText: () -> Unit,
    onRequestMicPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.typedText,
                onValueChange = onTypedTextChanged,
                modifier = Modifier.weight(1f),
                minLines = 1,
                maxLines = 3,
                placeholder = { Text("Ask Nova anything") },
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(onClick = onSendText, enabled = state.typedText.isNotBlank()) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onStopPressed) {
                Icon(Icons.Default.Stop, contentDescription = "Stop")
                Spacer(Modifier.width(8.dp))
                Text("Stop")
            }
            FilledIconButton(
                onClick = if (state.microphonePermissionGranted) onMicPressed else onRequestMicPermission,
                modifier = Modifier.size(76.dp)
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Talk to Nova",
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
private fun ConnectedAccountsScreen(
    state: NovaUiState,
    onRequestMicPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Connected Accounts", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        ConnectionCard(
            title = "Microphone",
            detail = if (state.microphonePermissionGranted) "Enabled for speech recognition" else "Permission required for voice input",
            connected = state.microphonePermissionGranted,
            actionLabel = "Enable",
            onAction = onRequestMicPermission
        )
        ConnectionCard(
            title = "OpenAI",
            detail = if (state.demoMode) "Add OPENAI_API_KEY in local.properties" else "Configured for live AI chat",
            connected = !state.demoMode,
            actionLabel = null,
            onAction = {}
        )
        ConnectionCard(
            title = "Gmail",
            detail = "Read-only OAuth integration is planned for Phase 3",
            connected = false,
            actionLabel = null,
            onAction = {}
        )
        ConnectionCard(
            title = "Notifications",
            detail = "Notification access indexing is planned for Phase 4",
            connected = false,
            actionLabel = null,
            onAction = {}
        )
    }
}

@Composable
private fun ConnectionCard(
    title: String,
    detail: String,
    connected: Boolean,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(
                    detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                AssistChip(
                    onClick = {},
                    label = { Text(if (connected) "Connected" else "Not connected") }
                )
                if (actionLabel != null && !connected) {
                    Button(onClick = onAction) { Text(actionLabel) }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    state: NovaUiState,
    onClearConversation: () -> Unit,
    onApiKeyDraftChanged: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onClearApiKey: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        SettingsRow("AI model", if (state.demoMode) "Demo assistant" else "OpenAI live model")
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("OpenAI API Key", fontWeight = FontWeight.Bold)
                Text(
                    if (state.apiKeySaved) {
                        "A key is saved securely on this device."
                    } else {
                        "No key saved. Nova will use demo mode."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
                OutlinedTextField(
                    value = state.apiKeyDraft,
                    onValueChange = onApiKeyDraftChanged,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    placeholder = { Text("Paste OpenAI API key") },
                    shape = RoundedCornerShape(8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onSaveApiKey,
                        enabled = state.apiKeyDraft.isNotBlank()
                    ) {
                        Text("Save key")
                    }
                    OutlinedButton(
                        onClick = onClearApiKey,
                        enabled = state.apiKeySaved
                    ) {
                        Text("Clear key")
                    }
                }
            }
        }
        SettingsRow("Voice", "Android TextToSpeech default")
        SettingsRow("Speech speed", "System default")
        SettingsRow("Theme", "Follows device")
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Privacy Controls", fontWeight = FontWeight.Bold)
                Text(
                    "Phase 1 stores this conversation in memory only. Room, memory management, Gmail cache, and notification cache controls are planned next.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onClearConversation) {
                    Text("Clear conversation")
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(title: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(value, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
    }
}

private val NovaDestination.icon: ImageVector
    get() = when (this) {
        NovaDestination.Home -> Icons.Default.Home
        NovaDestination.Accounts -> Icons.Default.AccountCircle
        NovaDestination.Settings -> Icons.Default.Settings
    }
