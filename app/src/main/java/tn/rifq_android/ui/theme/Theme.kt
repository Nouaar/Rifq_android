package tn.rifq_android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    secondary = OrangeLight,
    tertiary = OrangeAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = OrangeAccent,
    secondary = OrangeLight,
    tertiary = OrangePrimary,
    background = Color(0xFFF5F5F7),  // PageBackground
    surface = Color(0xFFFFFFFF),     // CardBackground
    surfaceVariant = Color(0xFFEFDBD1), // HeaderBackground
    onPrimary = Color.White,         // ChipSelectedText
    onSecondary = Color(0xFF1C1C1E), // TextPrimary
    onTertiary = Color(0xFF1C1C1E),  // TextPrimary
    onBackground = Color(0xFF1C1C1E), // TextPrimary
    onSurface = Color(0xFF1C1C1E),   // TextPrimary
    onSurfaceVariant = Color(0xFF8E8E93), // TextSecondary
    error = ErrorRed
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
