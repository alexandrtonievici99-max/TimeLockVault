package com.example.timelockvault.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VaultDarkColors = darkColorScheme(
    primary = Color(0xFFB9A7FF),
    secondary = Color(0xFF8CA5FF),
    background = Color(0xFF0C1117),
    surface = Color(0xFF111827),
    onPrimary = Color(0xFF111827),
    onBackground = Color(0xFFF4F7FF),
    onSurface = Color(0xFFF4F7FF),
    error = Color(0xFFFF5A5A)
)

@Composable
fun TimeLockVaultTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VaultDarkColors,
        content = content
    )
}
