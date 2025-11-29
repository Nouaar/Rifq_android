package tn.rifq_android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import tn.rifq_android.R
import tn.rifq_android.ui.components.BackgroundBox

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
    background = Color.White,  // White background - paw print overlay shows through
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

    // Select background image and color based on theme
    val backgroundColor = if (darkTheme) DarkBackground else Color.White
    val backgroundImageRes = if (darkTheme) R.drawable.darkmode else R.drawable.paw_background

    // Wrap with background image (under layer)
    // The background image appears as an under layer on all screens automatically
    // Light mode: paw_background.png, Dark mode: darkmode.png
    BackgroundBox(
        backgroundColor = backgroundColor,
        backgroundImageRes = backgroundImageRes
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
