package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val NexoraColorScheme = darkColorScheme(
    primary = NexoraBluePrimary,
    onPrimary = Color.White,
    primaryContainer = NexoraBlueDark,
    onPrimaryContainer = Color.White,
    secondary = NexoraCyanAccent,
    onSecondary = NavyBackground,
    secondaryContainer = NavyCardElevated,
    onSecondaryContainer = NexoraCyanAccent,
    tertiary = NexoraGreenSuccess,
    onTertiary = Color.White,
    background = NavyBackground,
    onBackground = TextWhite,
    surface = NavySurface,
    onSurface = TextLight,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = NavyCardBorder,
    outlineVariant = Color(0xFF1E2E4E),
    error = NexoraRedAlert,
    onError = Color.White
)

val NexoraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // NEXORA design is dark-navy-first as requested
    dynamicColor: Boolean = false, // Keep consistent brand navy blue styling
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NexoraColorScheme,
        typography = Typography,
        shapes = NexoraShapes,
        content = content
    )
}
