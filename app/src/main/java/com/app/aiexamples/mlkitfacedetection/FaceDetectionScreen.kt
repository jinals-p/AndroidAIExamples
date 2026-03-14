package com.app.aiexamples.mlkitfacedetection

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.aiexamples.core.AppHeader
import com.app.aiexamples.core.openAppSettings
import com.app.aiexamples.core.theme.Background
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.app.aiexamples.core.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceDetectionScreen(onBackCallback: () -> Unit) {

    val viewModel: FaceDetectionViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val torchEnabled by viewModel.torchEnabled.collectAsStateWithLifecycle()

    // ── Permission ────────────────────────────────────────
    val cameraPermission = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }
    KeepScreenOn()

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Background)) {
        AppHeader("Face Detection", "Powered by ML Kit", onBackCallback)
        when {
            cameraPermission.status.isGranted -> {
                FaceDetectionContent(state = state, torchEnabled = torchEnabled, onFacesDetected = { faces, w, h ->
                    viewModel.onFacesDetected(faces, w, h)
                }, onTorchToggle = { viewModel.toggleTorch() }, viewModel = viewModel)
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

// ── Main Content ──────────────────────────────────────────
@Composable
fun FaceDetectionContent(state: FaceDetectionState, torchEnabled: Boolean, onFacesDetected: (List<DetectedFace>, Int, Int) -> Unit, onTorchToggle: () -> Unit, viewModel: FaceDetectionViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Camera Preview ────────────────────────────────
        FaceCameraPreview(modifier = Modifier.fillMaxSize(), torchEnabled = torchEnabled, onFacesDetected = onFacesDetected)

        // ── Face overlay drawn on top of camera ──────────
        FaceOverlay(state = state, modifier = Modifier.fillMaxSize())

        // ── Dark gradient at top ──────────────────────────
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent))))

        // ── Dark gradient at bottom ───────────────────────
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .align(Alignment.BottomCenter)
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)))))

        // ── Top Bar ───────────────────────────────────────
        FaceTopBar(state = state, torchEnabled = torchEnabled, onTorchToggle = onTorchToggle, modifier = Modifier.align(Alignment.TopCenter))

        // ── Bottom Info Panel ─────────────────────────────
        AnimatedVisibility(visible = state is FaceDetectionState.FacesDetected,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)) {
            if (state is FaceDetectionState.FacesDetected) {
                FaceInfoPanel(state = state, viewModel = viewModel)
            }
        }

        // ── No face hint ──────────────────────────────────
        AnimatedVisibility(visible = state is FaceDetectionState.NoFace || state is FaceDetectionState.Idle, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.align(Alignment.Center)) {
            Text(text = "Point camera at a face", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp, textAlign = TextAlign.Center)
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────
@Composable
fun FaceTopBar(state: FaceDetectionState, torchEnabled: Boolean, onTorchToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column {
            //Text(text = "Face Detection", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            // Face count badge
            val faceCount = if (state is FaceDetectionState.FacesDetected) state.faces.size else 0
            Text(text = if (faceCount > 0) "$faceCount face${if (faceCount > 1) "s" else ""} detected"
            else "No face detected", color = if (faceCount > 0) AccentGreen else Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        }

        // Torch button
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

// ── Face Info Panel ───────────────────────────────────────
@Composable
fun FaceInfoPanel(state: FaceDetectionState.FacesDetected, viewModel: FaceDetectionViewModel) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Show info for each face
        state.faces.forEachIndexed { index, face ->
            FaceCard(face = face, faceIndex = index + 1, viewModel = viewModel, showIndex = state.faces.size > 1)
        }
    }
}

// ── Single Face Card ──────────────────────────────────────
@Composable
fun FaceCard(face: DetectedFace, faceIndex: Int, viewModel: FaceDetectionViewModel, showIndex: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Card.copy(alpha = 0.95f)), border = BorderStroke(1.dp, Border), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Face header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Face avatar circle
                    Box(modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentPurple.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Text(text = "👤", fontSize = 16.sp)
                    }
                    Text(text = if (showIndex) "Face $faceIndex" else "Face", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // Emotion badge
                EmotionBadge(emotion = viewModel.getEmotion(face.smilingProbability))
            }

            HorizontalDivider(color = Border)

            // Stats row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FaceStat(label = "Right Eye", value = viewModel.getEyeState(face.leftEyeOpenProbability), emoji = if ((face.leftEyeOpenProbability ?: 0f) > 0.5f) "👁️" else "😑")
                FaceStatDivider()
                FaceStat(label = "Left Eye", value = viewModel.getEyeState(face.rightEyeOpenProbability), emoji = if ((face.rightEyeOpenProbability ?: 0f) > 0.5f) "👁️" else "😑")
                FaceStatDivider()
                FaceStat(label = "Direction", value = viewModel.getHeadDirection(face.headEulerAngleY), emoji = "🧭")
            }

            // Smile probability bar
            face.smilingProbability?.let { prob ->
                SmileProbabilityBar(probability = prob)
            }
        }
    }
}

// ── Emotion Badge ─────────────────────────────────────────
@Composable
fun EmotionBadge(emotion: String) {
    val color = when {
        emotion.contains("Smiling") -> AccentGreen
        emotion.contains("Neutral") -> AccentPurple
        else -> TextMuted
    }
    Box(modifier = Modifier
        .clip(RoundedCornerShape(20.dp))
        .background(color.copy(alpha = 0.15f))
        .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        .padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text = emotion, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Single Face Stat ──────────────────────────────────────
@Composable
fun FaceStat(label: String, value: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = emoji, fontSize = 18.sp)
        Text(text = value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        Text(text = label, color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun FaceStatDivider() {
    Box(modifier = Modifier
        .width(1.dp)
        .height(40.dp)
        .background(Border))
}

// ── Smile Probability Bar ─────────────────────────────────
@Composable
fun SmileProbabilityBar(probability: Float) {
    val animatedProb by animateFloatAsState(targetValue = probability, animationSpec = tween(300), label = "smileProb")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Smile probability", color = TextMuted, fontSize = 11.sp)
            Text(text = "${(probability * 100).toInt()}%", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
        // Progress bar
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Border)) {
            Box(modifier = Modifier
                .fillMaxWidth(animatedProb)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(AccentGreen))
        }
    }
}

// ── Permission Views (reused from scanner) ────────────────
@Composable
fun PermissionRationaleView(onRequestClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = "📷", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Camera Access Needed", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Camera access is required to detect faces in real time.", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestClick, colors = ButtonDefaults.buttonColors(containerColor = AccentPurple), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Allow Camera", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PermissionDeniedView(onRequestClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = "🔒", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Camera Permission Denied", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Please enable camera permission from Settings to use face detection.", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { openAppSettings(context) }, colors = ButtonDefaults.buttonColors(containerColor = AccentPurple), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Open Settings", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}