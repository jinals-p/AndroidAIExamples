package com.app.aiexamples.core

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.aiexamples.core.theme.AccentPurple
import com.app.aiexamples.core.theme.Surface
import com.app.aiexamples.core.theme.TextMuted
import com.app.aiexamples.core.theme.TextPrimary
import ir.kaaveh.sdpcompose.sdp
import ir.kaaveh.sdpcompose.ssp

// ── Top App Bar used in every module ─────────────────────
@Composable
fun AppHeader(title: String, subtitle: String = "", onBackClick: (() -> Unit)? = null) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(Surface)
        .padding(horizontal = 8.sdp, vertical = 8.sdp)) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBackIosNew, modifier = Modifier
                    .size(22.sdp)
                    .padding(2.sdp), contentDescription = "Localized description", tint = Color.White)
            }
            Spacer(Modifier.width(4.sdp))
        }
        Column {
            Text(text = title, color = TextPrimary, fontSize = 14.ssp, fontWeight = FontWeight.ExtraBold)
            Text(text = subtitle, color = TextMuted, fontSize = 12.ssp)
        }
    }
}

// ── Status badge (shows "Scanning...", "Detected!" etc) ───
@Composable
fun StatusBadge(text: String, color: Color = AccentPurple) {
    Box(modifier = Modifier
        .clip(RoundedCornerShape(20.dp))
        .background(color.copy(alpha = 0.15f))
        .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        .padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(text = text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Loading indicator with message ───────────────────────
@Composable
fun LoadingView(message: String = "Processing...") {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(color = AccentPurple)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = message, color = TextMuted, fontSize = 14.sp)
    }
}

// ── Permission denied screen ──────────────────────────────
@Composable
fun PermissionDeniedView(permission: String, onRequestClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier
        .fillMaxSize()
        .padding(32.dp)) {
        Text(text = "🔒", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "$permission Permission Required", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "This feature needs $permission access to work.", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestClick, colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)) {
            Text("Grant Permission", color = Color.White)
        }
    }
}

fun openAppSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}