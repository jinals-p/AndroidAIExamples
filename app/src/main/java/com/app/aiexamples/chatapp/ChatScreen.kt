package com.app.aiexamples.chatapp

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.aiexamples.chatapp.viewmodels.AnalyzeState
import com.app.aiexamples.chatapp.viewmodels.ChatViewModel
import com.app.aiexamples.core.theme.AIExamplesTheme
import com.app.aiexamples.core.theme.AccentPurple
import com.app.aiexamples.core.theme.Background
import com.app.aiexamples.core.theme.Border
import com.app.aiexamples.core.theme.BorderBright
import com.app.aiexamples.core.theme.Card
import com.app.aiexamples.core.theme.CardAlt
import com.app.aiexamples.core.theme.Surface
import com.app.aiexamples.core.theme.TextGreen
import com.app.aiexamples.core.theme.TextMuted
import com.app.aiexamples.core.theme.TextPrimary
import ir.kaaveh.sdpcompose.sdp
import ir.kaaveh.sdpcompose.ssp
import java.util.UUID

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel(), onBackCallback: () -> Unit) {

    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val loadingState by viewModel.analyzeState.collectAsStateWithLifecycle()

    var prompt by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AIExamplesTheme {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(Background)) {
            // Header
            AppHeader()
            // Chat List
            ContainerUi(messages, listState, modifier = Modifier.weight(1f))
            // Input
            InputUi(prompt, loadingState, onValueChange = { prompt = it }) {
                viewModel.sendMessage(prompt)
                prompt = ""
                keyboardController?.hide()
            }
        }
    }
}

@Composable
fun AppHeader() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(54.sdp)
        .background(color = Surface)) {
        Row(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.sdp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Star,
                contentDescription = "Star",
                tint = TextPrimary,
                modifier = Modifier
                    .size(42.sdp)
                    .padding(8.sdp)
                    .background(color = AccentPurple, shape = RoundedCornerShape(18.sdp)))
            Spacer(modifier = Modifier.width(5.sdp))
            Column {
                Text(text = "AI Assistant", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 14.ssp)
                Text(text = "⬤ Online", color = TextGreen, fontSize = 11.ssp)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* Handle click */ }) {
                Icon(imageVector = Icons.Default.MoreHoriz, modifier = Modifier.size(22.sdp), contentDescription = "Localized description", tint = Color.White)
            }
        }
    }
}

@Composable
fun ContainerUi(messages: List<Message>, listState: LazyListState, modifier: Modifier) {
    LazyColumn(state = listState, modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 16.dp  // space for input bar at bottom
    ), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items = messages, key = { it.id }) { message ->
            ChatBubble(message = message)
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    // Entire row flips alignment based on isUser
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (message.isUser) {
        Arrangement.End    // user → right side
    } else {
        Arrangement.Start  // AI → left side
    }, verticalAlignment = Alignment.Top) {

        // AI avatar — only show for AI messages
        if (!message.isUser) {
            Box(modifier = Modifier
                .size(28.sdp)
                .clip(CircleShape)
                .background(AccentPurple), contentAlignment = Alignment.Center) {
                Text(text = "AI", color = Color.White, fontSize = 10.ssp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Bubble
        Box(modifier = Modifier
            .widthIn(max = 280.dp)  // max bubble width
            .clip(if (message.isUser) {
                // User — flat top-right
                RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
            } else {
                // AI — flat top-left
                RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
            })
            .background(if (message.isUser) Card
            else CardAlt)
            .padding(horizontal = 14.dp, vertical = 10.dp)) {
            if (message.isLoading) {
                // Typing indicator — 3 dots
                TypingIndicator()
            } else {
                Text(text = message.text, color = if (message.isUser) Color.White else TextPrimary, fontSize = 14.sp, lineHeight = 20.ssp)
            }
        }

        // User avatar spacer — keeps bubble from touching screen edge
        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

// Animated typing indicator
@Composable
fun TypingIndicator() {
    val dots = listOf(0, 200, 400)  // animation delay per dot
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        dots.forEach { delay ->
            val infiniteTransition = rememberInfiniteTransition(label = "dot")
            val offsetY by infiniteTransition.animateFloat(initialValue = 0f,
                targetValue = -6f,
                animationSpec = infiniteRepeatable(animation = tween(500, delayMillis = delay), repeatMode = RepeatMode.Reverse),
                label = "bounce")
            Box(modifier = Modifier
                .size(7.dp)
                .offset(y = offsetY.dp)
                .clip(CircleShape)
                .background(TextMuted))
        }
    }
}

@Composable
fun InputUi(prompt: String, loadingState: AnalyzeState, onValueChange: (String) -> Unit, onSendClick: () -> Unit) {
    val localFocusManager = LocalFocusManager.current

    Box(modifier = Modifier
        .fillMaxWidth()
        .background(color = Surface)) {

        Row(modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(8.sdp), verticalAlignment = Alignment.CenterVertically) {

            OutlinedTextField(value = prompt,
                onValueChange = onValueChange,
                modifier = Modifier.weight(weight = 1f),
                placeholder = { Text("Message AI", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BorderBright,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentPurple,
                    focusedContainerColor = Card,
                    unfocusedContainerColor = Card),
                shape = RoundedCornerShape(percent = 50),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))

            Spacer(modifier = Modifier.width(8.sdp))

            IconButton(modifier = Modifier.background(color = if (loadingState != AnalyzeState.Loading) AccentPurple else TextMuted, shape = RoundedCornerShape(percent = 50)), onClick = {
                localFocusManager.clearFocus(true)
                onSendClick()
            }, enabled = loadingState != AnalyzeState.Loading) {
                Icon(imageVector = Icons.Default.Send, modifier = Modifier
                    .size(45.sdp)
                    .padding(8.dp), contentDescription = "Localized description", tint = Color.White)
            }
        }
    }
}

data class Message(val id: String = UUID.randomUUID().toString(), val text: String, val isUser: Boolean, val isLoading: Boolean = false)