package com.example.rem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RelayColors = lightColorScheme(
    primary = Color(0xFF285CE6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7EDFF),
    onPrimaryContainer = Color(0xFF17368A),
    secondary = Color(0xFF56627D),
    background = Color(0xFFFBFAFD),
    surface = Color(0xFFFBFAFD),
    onSurface = Color(0xFF20242B),
    outline = Color(0xFFBFC7D4)
)

@Composable
fun RemTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RelayColors, typography = Typography, content = content)
}
