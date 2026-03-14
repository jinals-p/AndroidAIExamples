package com.app.aiexamples.mlkitimagelabeling

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.aiexamples.core.AppHeader
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.app.aiexamples.core.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImageLabelingScreen(onBackCallback: () -> Unit) {

    val viewModel: ImageLabelingViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val torchEnabled by viewModel.torchEnabled.collectAsStateWithLifecycle()
    val confidenceThreshold by viewModel.confidenceThreshold.collectAsStateWithLifecycle()

    KeepScreenOn()

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Background)) {
        AppHeader("Image Labeling", "Powered by ML Kit", onBackCallback)
        when {
            cameraPermission.status.isGranted -> {
                ImageLabelingContent(state = state, torchEnabled = torchEnabled, confidenceThreshold = confidenceThreshold, onLabelsDetected = { labels ->
                    val withEmoji = labels.map {
                        it.copy(emoji = viewModel.getEmojiForLabel(it.text))
                    }
                    viewModel.onLabelsDetected(withEmoji)
                }, onTorchToggle = { viewModel.toggleTorch() }, onThresholdChange = { viewModel.setConfidenceThreshold(it) }, viewModel = viewModel)
            }

            cameraPermission.status.shouldShowRationale -> {
                PermissionRationaleView(onRequestClick = { cameraPermission.launchPermissionRequest() })
            }

            else -> {
                PermissionDeniedView(onRequestClick = { cameraPermission.launchPermissionRequest() })
            }
        }
    }
}

// ── Main Content ──────────────────────────────────────────
@Composable
fun ImageLabelingContent(state: ImageLabelingState,
                         torchEnabled: Boolean,
                         confidenceThreshold: Float,
                         onLabelsDetected: (List<ImageLabel>) -> Unit,
                         onTorchToggle: () -> Unit,
                         onThresholdChange: (Float) -> Unit,
                         viewModel: ImageLabelingViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Camera ────────────────────────────────────────
        ImageLabelingCameraPreview(modifier = Modifier.fillMaxSize(), torchEnabled = torchEnabled, confidenceThreshold = confidenceThreshold, onLabelsDetected = onLabelsDetected)

        // ── Top gradient ──────────────────────────────────
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent))))

        // ── Bottom gradient ───────────────────────────────
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .align(Alignment.BottomCenter)
            .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f)))))

        // ── Top Bar ───────────────────────────────────────
        ImageLabelingTopBar(state = state, torchEnabled = torchEnabled, onTorchToggle = onTorchToggle, modifier = Modifier.align(Alignment.TopCenter))

        // ── No labels hint ────────────────────────────────
        AnimatedVisibility(visible = state is ImageLabelingState.NoLabel || state is ImageLabelingState.Idle, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.Center)) {
            Text(text = "Point camera at anything", color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp, textAlign = TextAlign.Center)
        }

        // ── Bottom Panel ──────────────────────────────────
        Column(modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Confidence threshold slider
            ConfidenceSlider(threshold = confidenceThreshold, onThresholdChange = onThresholdChange)

            // Labels chips
            AnimatedVisibility(visible = state is ImageLabelingState.LabelsDetected,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()) {
                if (state is ImageLabelingState.LabelsDetected) {
                    LabelsPanel(labels = state.labels)
                }
            }
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────
@Composable
fun ImageLabelingTopBar(state: ImageLabelingState, torchEnabled: Boolean, onTorchToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column {
            //Text(text = "Image Labeling", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = when (state) {
                is ImageLabelingState.LabelsDetected -> "${state.labels.size} labels found"
                is ImageLabelingState.NoLabel -> "Nothing recognized"
                else -> "Analyzing..."
            }, color = when (state) {
                is ImageLabelingState.LabelsDetected -> AccentGreen
                else -> Color.White.copy(alpha = 0.6f)
            }, fontSize = 13.sp)
        }

        IconButton(onClick = onTorchToggle, modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (torchEnabled) AccentPurple
            else Color.White.copy(alpha = 0.15f))) {
            Icon(imageVector = if (torchEnabled) Icons.Default.FlashOn
            else Icons.Default.FlashOff, contentDescription = "Torch", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

// ── Confidence Threshold Slider ───────────────────────────
@Composable
fun ConfidenceSlider(threshold: Float, onThresholdChange: (Float) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Card.copy(alpha = 0.9f)), border = BorderStroke(1.dp, Border), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Min Confidence", color = TextMuted, fontSize = 12.sp)
                Text(text = "${(threshold * 100).toInt()}%", color = AccentPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(value = threshold,
                onValueChange = onThresholdChange,
                valueRange = 0.3f..0.95f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple, inactiveTrackColor = Border))
        }
    }
}

// ── Labels Panel ──────────────────────────────────────────
@Composable
fun LabelsPanel(labels: List<ImageLabel>) {
    Card(colors = CardDefaults.cardColors(containerColor = Card.copy(alpha = 0.95f)), border = BorderStroke(1.dp, Border), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "DETECTED LABELS", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.5.sp)

            // Horizontal scrollable chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(labels) { label ->
                    LabelChip(label = label)
                }
            }

            HorizontalDivider(color = Border)

            // Top label with full confidence bar
            labels.firstOrNull()?.let { topLabel ->
                TopLabelRow(label = topLabel)
            }
        }
    }
}

// ── Single Label Chip ─────────────────────────────────────
@Composable
fun LabelChip(label: ImageLabel) {
    val confidence = label.confidence
    val color = when {
        confidence > 0.85f -> AccentGreen
        confidence > 0.70f -> AccentPurple
        else -> AccentBlue
    }

    Box(modifier = Modifier
        .clip(RoundedCornerShape(20.dp))
        .background(color.copy(alpha = 0.12f))
        .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        .padding(horizontal = 12.dp, vertical = 8.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label.emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label.text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = "${(confidence * 100).toInt()}%", color = color.copy(alpha = 0.7f), fontSize = 10.sp)
        }
    }
}

// ── Top Label with confidence bar ────────────────────────
@Composable
fun TopLabelRow(label: ImageLabel) {
    val animatedConfidence by animateFloatAsState(targetValue = label.confidence, animationSpec = tween(400), label = "confidence")

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = label.emoji, fontSize = 28.sp)

        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = label.text, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "${(label.confidence * 100).toInt()}%", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Confidence bar
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Border)) {
                Box(modifier = Modifier
                    .fillMaxWidth(animatedConfidence)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.horizontalGradient(colors = listOf(AccentPurple, AccentGreen))))
            }
        }
    }
}

// ── Keep Screen On ────────────────────────────────────────
@Composable
fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as android.app.Activity).window
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

// ── Permission Views ──────────────────────────────────────
@Composable
fun PermissionRationaleView(onRequestClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = "📷", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Camera Access Needed", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Camera is required to label and identify objects in real time.", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestClick, colors = ButtonDefaults.buttonColors(containerColor = AccentPurple), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Allow Camera", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PermissionDeniedView(onRequestClick: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = "🔒", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Camera Permission Denied", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Please enable camera permission from Settings to use image labeling.", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }, colors = ButtonDefaults.buttonColors(containerColor = AccentPurple), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Open Settings", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}