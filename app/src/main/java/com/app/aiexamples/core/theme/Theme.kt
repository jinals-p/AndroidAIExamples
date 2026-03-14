package com.app.aiexamples.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Main theme wrapper ────────────────────────────────────
@Composable
fun AIExamplesTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = AccentPurple,
        secondary = AccentBlue,
        tertiary = AccentPink,
        background = Background,
        surface = Surface,
        surfaceVariant = Card,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = TextPrimary,
        onSurface = TextPrimary,
        outline = Border,
        outlineVariant = BorderBright), typography = Typography, content = content)
}