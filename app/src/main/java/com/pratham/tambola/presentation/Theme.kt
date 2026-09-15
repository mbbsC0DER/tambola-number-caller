package com.pratham.tambola.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.pratham.tambola.data.ThemePreference

private val LightColors = lightColorScheme(
    primary = Color(0xFF255B45), onPrimary = Color.White,
    primaryContainer = Color(0xFFD8EDB8), onPrimaryContainer = Color(0xFF143D2B),
    secondary = Color(0xFF58634D), secondaryContainer = Color(0xFFE0E8D5),
    background = Color(0xFFF7F8F2), surface = Color(0xFFF7F8F2),
    surfaceContainer = Color(0xFFEDF0E7), surfaceContainerHigh = Color(0xFFE6EADF),
    onSurface = Color(0xFF20271F), onSurfaceVariant = Color(0xFF596154),
    outlineVariant = Color(0xFFD5DBCD),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFACD4AE), onPrimary = Color(0xFF113825),
    primaryContainer = Color(0xFF2D5138), onPrimaryContainer = Color(0xFFD8EDB8),
    background = Color(0xFF131A15), surface = Color(0xFF131A15),
    surfaceContainer = Color(0xFF202A22), surfaceContainerHigh = Color(0xFF2B352D),
)

@Composable
fun TambolaTheme(preference: ThemePreference, content: @Composable () -> Unit) {
    val dark = when (preference) { ThemePreference.SYSTEM -> isSystemInDarkTheme(); ThemePreference.LIGHT -> false; ThemePreference.DARK -> true }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = Typography(),
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)),
        content = content,
    )
}
