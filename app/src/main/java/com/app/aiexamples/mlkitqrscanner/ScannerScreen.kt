package com.app.aiexamples.mlkitqrscanner

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.aiexamples.core.AppHeader
import com.app.aiexamples.core.openAppSettings
import com.app.aiexamples.core.theme.AccentGreen
import com.app.aiexamples.core.theme.AccentPurple
import com.app.aiexamples.core.theme.Background
import com.app.aiexamples.core.theme.Border
import com.app.aiexamples.core.theme.Card
import com.app.aiexamples.core.theme.TextMuted
import com.app.aiexamples.core.theme.TextPrimary
import com.app.aiexamples.mlkitfacedetection.KeepScreenOn
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(onBackCallback: () -> Unit) {

    val viewModel: ScannerViewModel = viewModel()
    val scannerState by viewModel.scannerState.collectAsStateWithLifecycle()
    val torchEnabled by viewModel.torchEnabled.collectAsStateWithLifecycle()

    // ── Camera Permission ─────────────────────────────────
    val cameraPermission = rememberPermissionState(permission = Manifest.permission.CAMERA)

    // Request permission on first launch
    LaunchedEffect(Unit) {
        // Only request — don't start scanning here
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    KeepScreenOn()

    LaunchedEffect(cameraPermission.status.isGranted) {
        // React to permission result — start scanning
        if (cameraPermission.status.isGranted) {
            viewModel.startScanning()
        }
    }

    // ── UI ────────────────────────────────────────────────
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Background)) {
        AppHeader("Qr / Barcode Scanner", "Powered by ML Kit", onBackCallback)
        when {
            // Permission granted — show camera
            cameraPermission.status.isGranted -> {
                ScannerContent(scannerState = scannerState, torchEnabled = torchEnabled, onBarcodeDetected = { value, type ->
                    viewModel.onBarcodeDetected(value, type)
                }, onTorchToggle = { viewModel.toggleTorch() }, onReset = { viewModel.resetScanner() })
            }

            // Permission denied with rationale — explain why
            cameraPermission.status.shouldShowRationale -> {
                PermissionRationaleView(onRequestClick = { cameraPermission.launchPermissionRequest() })
            }

            // Permission permanently denied
            else -> {
                PermissionDeniedView()
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

// ── Main Scanner Content ──────────────────────────────────
@Composable
fun ScannerContent(scannerState: ScannerState, torchEnabled: Boolean, onBarcodeDetected: (String, String) -> Unit, onTorchToggle: () -> Unit, onReset: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Camera Preview (full screen) ──────────────────
        CameraPreview(modifier = Modifier.fillMaxSize(), torchEnabled = torchEnabled, onBarcodeDetected = onBarcodeDetected)

        // ── Dark overlay on top of camera ─────────────────
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000)))

        // ── Top Bar ───────────────────────────────────────
        ScannerTopBar(torchEnabled = torchEnabled, onTorchToggle = onTorchToggle, modifier = Modifier.align(Alignment.TopCenter))

        // ── Scan Frame in center ──────────────────────────
        ScanFrame(isScanned = scannerState is ScannerState.Scanned, modifier = Modifier.align(Alignment.Center))

        // ── Hint text ─────────────────────────────────────
        Text(text = if (scannerState is ScannerState.Scanned) "✅ Scanned successfully!"
        else "Point camera at a QR code or barcode",
            color = Color.White,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 160.dp)
                .padding(horizontal = 32.dp))

        // ── Result Card — slides up when scanned ──────────
        AnimatedVisibility(visible = scannerState is ScannerState.Scanned,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)) {
            if (scannerState is ScannerState.Scanned) {
                ResultCard(value = scannerState.value, type = scannerState.type, onScanAgain = onReset)
            }
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────
@Composable
fun ScannerTopBar(torchEnabled: Boolean, onTorchToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        //Text(text = "Scanner", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Torch toggle button
        IconButton(onClick = onTorchToggle, modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(50))
            .background(if (torchEnabled) AccentPurple
            else Color.White.copy(alpha = 0.15f))) {
            Icon(imageVector = if (torchEnabled) Icons.Default.FlashOn
            else Icons.Default.FlashOff, contentDescription = "Torch", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

// ── Animated Scan Frame ───────────────────────────────────
@Composable
fun ScanFrame(isScanned: Boolean, modifier: Modifier = Modifier) {
    val borderColor by animateColorAsState(targetValue = if (isScanned) AccentGreen else Color.White, animationSpec = tween(300), label = "borderColor")

    // Scanning line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLineY by infiniteTransition.animateFloat(initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "scanLine")

    Box(modifier = modifier.size(240.dp), contentAlignment = Alignment.TopStart) {
        // Corner brackets
        val cornerSize = 32.dp
        val strokeWidth = 3.dp

        // Top-left
        CornerBracket(Alignment.TopStart, cornerSize, strokeWidth, borderColor)
        // Top-right
        CornerBracket(Alignment.TopEnd, cornerSize, strokeWidth, borderColor)
        // Bottom-left
        CornerBracket(Alignment.BottomStart, cornerSize, strokeWidth, borderColor)
        // Bottom-right
        CornerBracket(Alignment.BottomEnd, cornerSize, strokeWidth, borderColor)

        // Animated scan line — only show when not scanned
        if (!isScanned) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 8.dp)
                .offset(y = (240 * scanLineY).dp)
                .background(AccentPurple.copy(alpha = 0.8f), RoundedCornerShape(1.dp)))
        }
    }
}

// ── Corner Bracket ────────────────────────────────────────
@Composable
fun BoxScope.CornerBracket(alignment: Alignment, size: androidx.compose.ui.unit.Dp, strokeWidth: androidx.compose.ui.unit.Dp, color: Color) {
    Box(modifier = Modifier
        .size(size)
        .align(alignment)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth.toPx())
            val s = size.toPx()
            val isLeft = alignment == Alignment.TopStart || alignment == Alignment.BottomStart
            val isTop = alignment == Alignment.TopStart || alignment == Alignment.TopEnd

            // Horizontal line
            drawLine(color = color,
                start = androidx.compose.ui.geometry.Offset(if (isLeft) 0f else s, if (isTop) 0f else s),
                end = androidx.compose.ui.geometry.Offset(if (isLeft) s else 0f, if (isTop) 0f else s),
                strokeWidth = strokeWidth.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round)
            // Vertical line
            drawLine(color = color,
                start = androidx.compose.ui.geometry.Offset(if (isLeft) 0f else s, if (isTop) 0f else s),
                end = androidx.compose.ui.geometry.Offset(if (isLeft) 0f else s, if (isTop) s else 0f),
                strokeWidth = strokeWidth.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}

// ── Result Card ───────────────────────────────────────────
@Composable
fun ResultCard(value: String, type: String, onScanAgain: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .navigationBarsPadding(),
        colors = CardDefaults.cardColors(containerColor = Card),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp)) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Type badge
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "RESULT", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AccentPurple.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(text = type, color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Scanned value
            Text(text = value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 4, overflow = TextOverflow.Ellipsis)

            HorizontalDivider(color = Border)

            // Scan again button
            Button(onClick = onScanAgain, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AccentPurple), shape = RoundedCornerShape(12.dp)) {
                Text(text = "Scan Again", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Permission Rationale ──────────────────────────────────
@Composable
fun PermissionRationaleView(onRequestClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = "📷", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Camera Access Needed", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "This app needs camera access to scan QR codes and barcodes.", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestClick, colors = ButtonDefaults.buttonColors(containerColor = AccentPurple), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Allow Camera", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Permission Permanently Denied ─────────────────────────
@Composable
fun PermissionDeniedView() {
    val context = LocalContext.current  // ✅ get context

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(text = "🔒", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Camera Permission Denied", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Camera permission was permanently denied. Please enable it manually from Settings.", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { openAppSettings(context) }, colors = ButtonDefaults.buttonColors(containerColor = AccentPurple), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Open Settings", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}