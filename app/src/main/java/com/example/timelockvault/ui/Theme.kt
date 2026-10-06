package com.example.timelockvault.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkVaultColors = darkColorScheme(
    primary = Color(0xFFB9A7FF),
    secondary = Color(0xFF8AA4FF),
    background = Color(0xFF0D1117),
    surface = Color(0xFF111827),
    onPrimary = Color(0xFF0D1117),
    onBackground = Color(0xFFF3F4F6),
    onSurface = Color(0xFFF3F4F6),
    error = Color(0xFFEF4444)
)

@Composable
fun TimeLockVaultTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkVaultColors,
        content = content
    )
}
