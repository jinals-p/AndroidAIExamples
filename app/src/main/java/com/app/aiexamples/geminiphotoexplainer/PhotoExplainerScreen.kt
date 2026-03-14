package com.app.aiexamples.geminiphotoexplainer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.aiexamples.core.AppHeader
import com.app.aiexamples.core.theme.AccentPink
import com.app.aiexamples.core.theme.AccentPurple
import com.app.aiexamples.core.theme.Background
import com.app.aiexamples.core.theme.Border
import com.app.aiexamples.core.theme.BorderBright
import com.app.aiexamples.core.theme.Card
import com.app.aiexamples.core.theme.TextMuted
import com.app.aiexamples.core.theme.TextPrimary
import com.app.aiexamples.geminiphotoexplainer.viewmodels.AnalyzeState
import com.app.aiexamples.geminiphotoexplainer.viewmodels.PhotoViewModel

// ── Root Composable ───────────────────────────────────────────────────────────
@Composable
fun PhotoExplainerScreen(viewModel: PhotoViewModel = viewModel(), onBackCallback: () -> Unit) {

    // Observe ViewModel states
    val bitmap by viewModel.selectedBitmap.collectAsStateWithLifecycle()
    val state by viewModel.analyzeState.collectAsStateWithLifecycle()

    // Local UI state
    var prompt by remember { mutableStateOf("What is in this image? Explain in detail.") }

    val context = LocalContext.current

    // ── Image Pickers ────────────────────────────────────────────────────────

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val stream = context.contentResolver.openInputStream(it)
            val bmp = BitmapFactory.decodeStream(stream)
            viewModel.setImage(bmp)
        }
    }

    // Camera picker
    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) { bmp: Bitmap? ->
        bmp?.let { viewModel.setImage(it) }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Background)
        .statusBarsPadding()
        .verticalScroll(rememberScrollState())   // scrollable screen
    ) {
        // Header
        AppHeader("⚡ AI Photo Explainer", "Powered by Gemini Vision", onBackCallback)

        // Image area
        ImageSection(bitmap = bitmap, onCameraClick = { cameraLauncher.launch(null) }, onGalleryClick = { galleryLauncher.launch("image/*") })

        Spacer(modifier = Modifier.height(16.dp))

        // Prompt input
        PromptInput(prompt = prompt, onPromptChange = { prompt = it })

        Spacer(modifier = Modifier.height(12.dp))

        // Analyze button
        AnalyzeButton(isLoading = state is AnalyzeState.Loading, enabled = bitmap != null, onClick = { viewModel.analyzeImage(prompt) })

        Spacer(modifier = Modifier.height(16.dp))

        // Result section — animates in when result arrives
        AnimatedVisibility(visible = state !is AnalyzeState.Idle, enter = fadeIn() + slideInVertically()) {
            ResultSection(state = state)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── Image Section ─────────────────────────────────────────────────────────────
@Composable
fun ImageSection(bitmap: Bitmap?, onCameraClick: () -> Unit, onGalleryClick: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {

        // Image preview box
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .border(width = 1.dp, color = if (bitmap != null) BorderBright else Border, shape = RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            if (bitmap != null) {
                // Show selected image
                Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Selected photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                // Placeholder
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🖼️", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "No image selected", color = TextMuted, fontSize = 14.sp)
                    Text(text = "Take a photo or pick from gallery", color = TextMuted, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Camera and Gallery buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Camera button
            Button(onClick = onCameraClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AccentPurple), shape = RoundedCornerShape(10.dp)) {
                Text("📷 Camera")
            }

            // Gallery button
            OutlinedButton(onClick = onGalleryClick, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, AccentPurple), shape = RoundedCornerShape(10.dp)) {
                Text("🖼️ Gallery", color = AccentPurple)
            }
        }
    }
}

// ── Prompt Input ──────────────────────────────────────────────────────────────
@Composable
fun PromptInput(prompt: String, onPromptChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "ASK ABOUT THE IMAGE", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. What is in this image?", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPurple,
                unfocusedBorderColor = Color(0xFF2A2A3A),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentPurple,
                focusedContainerColor = Card,
                unfocusedContainerColor = Card),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3)

        // Quick prompt suggestions
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Quick prompts:", color = TextMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("Describe this", "Is food healthy?", "What brand is this?", "Translate text", "Any issues?").forEach { suggestion ->
                SuggestionChip(onClick = { onPromptChange(suggestion) },
                    label = { Text(suggestion, fontSize = 11.sp, color = TextPrimary) },
                    border = BorderStroke(1.dp, Border),
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Card))
            }
        }
    }
}

// ── Analyze Button ────────────────────────────────────────────────────────────
@Composable
fun AnalyzeButton(isLoading: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentPink, disabledContainerColor = Color(0xFF2A2A3A)),
        shape = RoundedCornerShape(12.dp)) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Gemini is thinking...", color = Color.White)
        } else {
            Text(text = if (enabled) "⚡ Analyze with Gemini" else "Select an image first", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ── Result Section ────────────────────────────────────────────────────────────
@Composable
fun ResultSection(state: AnalyzeState) {
    Column(modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(Card)
        .border(width = 1.dp, color = when (state) {
            is AnalyzeState.Success -> AccentPurple
            is AnalyzeState.Error -> Color(0xFFFF6B6B)
            else -> Color(0xFF2A2A3A)
        }, shape = RoundedCornerShape(16.dp))
        .padding(16.dp)) {
        when (state) {

            is AnalyzeState.Loading -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = AccentPurple, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Analyzing image...", color = TextMuted, fontSize = 14.sp)
                }
            }

            is AnalyzeState.Success -> {
                Text(text = "GEMINI SAYS", color = AccentPurple, fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = state.result, color = TextPrimary, fontSize = 15.sp, lineHeight = 24.sp)
            }

            is AnalyzeState.Error -> {
                Text(text = "❌ ERROR", color = Color(0xFFFF6B6B), fontSize = 11.sp, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.message, color = Color(0xFFFF6B6B), fontSize = 14.sp)
            }

            else -> {}
        }
    }
}