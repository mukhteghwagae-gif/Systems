package systems.nzr1.ui.screens.assistant

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import systems.nzr1.domain.model.ChatMessage
import systems.nzr1.domain.model.MessageRole
import systems.nzr1.ui.components.GridBackground
import systems.nzr1.ui.components.SectionHeader
import systems.nzr1.ui.theme.*

@Composable
fun AssistantScreen(viewModel: AssistantViewModel = hiltViewModel()) {
    val messages   by viewModel.messages.collectAsState()
    val isLoading  by viewModel.isLoading.collectAsState()
    val apiKey     by viewModel.apiKey.collectAsState()
    var input      by remember { mutableStateOf("") }
    var showKeyDlg by remember { mutableStateOf(false) }
    val listState  = rememberLazyListState()
    val scope      = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    if (showKeyDlg) ApiKeyDialog(apiKey, onSave = {
        viewModel.saveApiKey(it); showKeyDlg = false
    }, onDismiss = { showKeyDlg = false })

    Box(modifier = Modifier.fillMaxSize().background(DeepVoid)) {
        GridBackground(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize()) {

            // Header
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Column {
                    Text("AI ASSISTANT", color = PurpleCore, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                    Text("Powered by Claude · NZRI Interface", color = TextMuted, fontSize = 9.sp)
                }
                Row {
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(Icons.Filled.DeleteSweep, "Clear", tint = TextMuted)
                    }
                    IconButton(onClick = { showKeyDlg = true }) {
                        Icon(Icons.Filled.Key, "API Key",
                            tint = if (apiKey.isNotBlank()) GreenCore else OrangeCore)
                    }
                }
            }

            if (apiKey.isBlank()) {
                Box(Modifier.fillMaxWidth().padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangeCore.copy(0.1f))
                    .border(1.dp, OrangeCore.copy(0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
                ) {
                    Text("⚠ Set your Anthropic API key to enable AI chat",
                        color = OrangeCore, fontSize = 11.sp)
                }
            }

            // Messages
            LazyColumn(
                state   = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(messages) { msg -> MessageBubble(msg) }
                if (isLoading) item { TypingIndicator() }
            }

            // Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 90.dp, top = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask NZRI anything…", color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PurpleCore,
                        unfocusedBorderColor = BorderDim,
                        cursorColor          = PurpleCore,
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        focusedContainerColor   = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                    ),
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (input.isNotBlank() && !isLoading) {
                            viewModel.sendMessage(input); input = ""
                        }
                    }),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    shape = RoundedCornerShape(12.dp),
                )
                FloatingActionButton(
                    onClick = {
                        if (input.isNotBlank() && !isLoading) {
                            viewModel.sendMessage(input); input = ""
                        }
                    },
                    containerColor = PurpleCore,
                    contentColor   = DeepVoid,
                    modifier       = Modifier.size(48.dp),
                    shape          = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Filled.Send, "Send", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.role == MessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(50))
                    .background(PurpleCore.copy(0.2f)).border(1.dp, PurpleCore.copy(0.4f), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) { Text("AI", color = PurpleCore, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(6.dp))
        }
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(
                    topStart = 12.dp, topEnd = 12.dp,
                    bottomStart = if (isUser) 12.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 12.dp,
                ))
                .background(
                    if (isUser) PurpleCore.copy(0.2f)
                    else if (msg.isError) RedCore.copy(0.15f)
                    else SurfaceCard
                )
                .border(
                    1.dp,
                    if (isUser) PurpleCore.copy(0.4f)
                    else if (msg.isError) RedCore.copy(0.3f)
                    else BorderDim,
                    RoundedCornerShape(
                        topStart = 12.dp, topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 12.dp,
                    )
                )
                .padding(10.dp)
        ) {
            Text(
                text     = msg.content,
                color    = if (msg.isError) RedCore else TextPrimary,
                fontSize = 13.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha by infiniteTransition.animateFloat(
        0.3f, 1f, label = "a",
        animationSpec = infiniteRepeatable(
            androidx.compose.animation.core.tween(600),
            androidx.compose.animation.core.RepeatMode.Reverse
        )
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp).clip(RoundedCornerShape(50)).background(PurpleCore.copy(0.2f)),
            contentAlignment = Alignment.Center) {
            Text("AI", color = PurpleCore, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(6.dp))
        Box(Modifier.clip(RoundedCornerShape(12.dp)).background(SurfaceCard).padding(12.dp)) {
            Text("NZRI is thinking…", color = PurpleCore.copy(alpha), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ApiKeyDialog(current: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var key     by remember { mutableStateOf(current) }
    var visible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceCard,
        title   = { Text("Anthropic API Key", color = CyanCore, fontWeight = FontWeight.Bold) },
        text    = {
            Column {
                Text("Enter your Claude API key. Get one at console.anthropic.com",
                    color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value         = key,
                    onValueChange = { key = it },
                    label         = { Text("sk-ant-…") },
                    singleLine    = true,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon  = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                "Toggle", tint = TextMuted)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanCore,
                        focusedTextColor   = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(key) }) {
                Text("SAVE", color = CyanCore, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = TextMuted) }
        },
    )
}
