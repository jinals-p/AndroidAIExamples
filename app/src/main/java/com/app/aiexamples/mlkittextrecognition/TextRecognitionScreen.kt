package com.app.aiexamples.mlkittextrecognition

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
fun TextRecognitionScreen(onBackCallback: () -> Unit) {

    val viewModel: TextRecognitionViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isFrozen by viewModel.isFrozen.collectAsStateWithLifecycle()
    val torchEnabled by viewModel.torchEnabled.collectAsStateWithLifecycle()
    val copiedSuccess by viewModel.copiedSuccess.collectAsStateWithLifecycle()

    val context = LocalContext.current

    KeepScreenOn()

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(cameraPermission.status.isGranted) {
        if (cameraPermission.status.isGranted) {
            viewModel.startScanning()
        }
    }

    // Auto reset copied badge
    LaunchedEffect(copiedSuccess) {
        if (copiedSuccess) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetCopied()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Background)) {
        AppHeader("Text Recognition", "Powered by ML Kit", onBackCallback)
        when {
            cameraPermission.status.isGranted -> {
                TextRecognitionContent(state = state, isFrozen = isFrozen, torchEnabled = torchEnabled, copiedSuccess = copiedSuccess, onTextDetected = { text, blocks ->
                    viewModel.onTextDetected(text, blocks)
                }, onTorchToggle = { viewModel.toggleTorch() }, onFreeze = { viewModel.freeze() }, onResume = { viewModel.startScanning() }, onCopy = { text ->
                    viewModel.copyToClipboard(context, text)
                }, onShare = { text ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share text"))
                })
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
fun TextRecognitionContent(state: TextRecognitionState,
                           isFrozen: Boolean,
                           torchEnabled: Boolean,
                           copiedSuccess: Boolean,
                           onTextDetected: (String, List<String>) -> Unit,
                           onTorchToggle: () -> Unit,
                           onFreeze: () -> Unit,
                           onResume: () -> Unit,
                           onCopy: (String) -> Unit,
                           onShare: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Camera Preview ────────────────────────────────
        TextCameraPreview(modifier = Modifier.fillMaxSize(), torchEnabled = torchEnabled, isFrozen = isFrozen, onTextDetected = onTextDetected)

        // ── Top gradient ──────────────────────────────────
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent))))

        // ── Bottom gradient ───────────────────────────────
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .align(Alignment.BottomCenter)
            .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)))))

        // ── Top Bar ───────────────────────────────────────
        TextTopBar(state = state, isFrozen = isFrozen, torchEnabled = torchEnabled, onTorchToggle = onTorchToggle, modifier = Modifier.align(Alignment.TopCenter))

        // ── Scan guide box in center ──────────────────────
        if (!isFrozen) {
            ScanGuideBox(hasText = state is TextRecognitionState.TextDetected, modifier = Modifier.align(Alignment.Center))
        }

        // ── Freeze button ─────────────────────────────────
        if (state is TextRecognitionState.TextDetected && !isFrozen) {
            Button(onClick = onFreeze,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 140.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(12.dp)) {
                Icon(imageVector = Icons.Default.PauseCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Capture Text", fontWeight = FontWeight.Bold)
            }
        }

        // ── Result Panel ──────────────────────────────────
        AnimatedVisibility(visible = isFrozen && state is TextRecognitionState.TextDetected,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)) {
            if (state is TextRecognitionState.TextDetected) {
                TextResultPanel(state = state, copiedSuccess = copiedSuccess, onCopy = onCopy, onShare = onShare, onScanAgain = onResume)
            }
        }

        // ── Copied success badge ──────────────────────────
        AnimatedVisibility(visible = copiedSuccess, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut(), modifier = Modifier
            .align(Alignment.Center)
            .offset(y = (-60).dp)) {
            Box(modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AccentGreen.copy(alpha = 0.9f))
                .padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(text = "✅ Copied to clipboard!", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────
@Composable
fun TextTopBar(state: TextRecognitionState, isFrozen: Boolean, torchEnabled: Boolean, onTorchToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column {
            //Text(text = "Text Recognition", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = when {
                isFrozen -> "📌 Text captured"
                state is TextRecognitionState.TextDetected -> "✅ Text detected"
                state is TextRecognitionState.NoText -> "No text found"
                else -> "Scanning..."
            }, color = when {
                isFrozen -> AccentTeal
                state is TextRecognitionState.TextDetected -> AccentGreen
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

// ── Scan Guide Box ────────────────────────────────────────
@Composable
fun ScanGuideBox(hasText: Boolean, modifier: Modifier = Modifier) {
    val color by animateColorAsState(targetValue = if (hasText) AccentGreen else Color.White.copy(alpha = 0.6f), label = "guideColor")

    Box(modifier = modifier
        .width(300.dp)
        .height(160.dp)
        .border(width = 2.dp, color = color, shape = RoundedCornerShape(8.dp))) {
        // Corner accents
        Text(text = if (hasText) "Text detected!" else "Align text here", color = color, fontSize = 12.sp, modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = 24.dp))
    }
}

// ── Result Panel ──────────────────────────────────────────
@Composable
fun TextResultPanel(state: TextRecognitionState.TextDetected, copiedSuccess: Boolean, onCopy: (String) -> Unit, onShare: (String) -> Unit, onScanAgain: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .navigationBarsPadding(),
        colors = CardDefaults.cardColors(containerColor = Card),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "DETECTED TEXT", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.5.sp)
                // Block count badge
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AccentPurple.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(text = "${state.blocks.size} block${if (state.blocks.size > 1) "s" else ""}", color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Scrollable text result
            Box(modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp, max = 160.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Surface)
                .padding(12.dp)
                .verticalScroll(rememberScrollState())) {
                Text(text = state.fullText, color = TextPrimary, fontSize = 14.sp, lineHeight = 22.sp)
            }

            HorizontalDivider(color = Border)

            // Action buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Copy button
                OutlinedButton(onClick = { onCopy(state.fullText) },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, if (copiedSuccess) AccentGreen else AccentPurple),
                    shape = RoundedCornerShape(10.dp)) {
                    Icon(imageVector = if (copiedSuccess) Icons.Default.Check
                    else Icons.Default.ContentCopy, contentDescription = null, tint = if (copiedSuccess) AccentGreen else AccentPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (copiedSuccess) "Copied!" else "Copy", color = if (copiedSuccess) AccentGreen else AccentPurple)
                }

                // Share button
                OutlinedButton(onClick = { onShare(state.fullText) }, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, AccentBlue), shape = RoundedCornerShape(10.dp)) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", color = AccentBlue)
                }

                // Scan again button
                Button(onClick = onScanAgain, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AccentPurple), shape = RoundedCornerShape(10.dp)) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rescan", color = Color.White)
                }
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
        Text(text = "Camera is required to scan and recognize text in real time.", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
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
        Text(text = "Please enable camera permission from Settings to use text recognition.", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
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