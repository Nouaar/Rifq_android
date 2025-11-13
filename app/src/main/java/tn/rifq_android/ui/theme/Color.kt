package tn.rifq_android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Theme-aware color properties
val PageBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val HeaderBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val CardBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val ScreenBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val LoginBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val InputBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant


private val LightPageBackground = Color(0xFFF5F5F7)
private val LightHeaderBackground = Color(0xFFEFDBD1)
private val LightCardBackground = Color(0xFFFFFFFF)
private val LightScreenBackground = Color(0xFFFBF4F2)
private val LightLoginBackground = Color(0xFFF8F2EE)
private val LightInputBackground = Color(0xFFF5F5F5)

// Primary Colors
val OrangeAccent = Color(0xFFD4845D)
val OrangeDark = Color(0xFFD4845D)
val OrangeLight = Color(0xFFFDB947)
val OrangePrimary = Color(0xFFFF7F32)
val OrangeSplash = Color(0xFFE58D4D)
val OrangeButton = Color(0xFFD07B4B)

// Pet Avatar Colors
val PetAvatarBrown = Color(0xFFD4A598)
val PetAvatarTan = Color(0xFFC9A88A)
val AvatarBackground = Color(0xFFE8C4B4)


private val LightTextPrimary = Color(0xFF1C1C1E)
private val LightTextSecondary = Color(0xFF8E8E93)
val TextLink = Color(0xFFFF7F32)

// Chip Colors
val ChipSelectedBg = Color(0xFFCD9B7F)
val ChipUnselectedBg = Color.White
val ChipSelectedText = Color.White
val ChipUnselectedText = Color(0xFFCD9B7F)

// Timeline Colors
val TimelineDot = Color(0xFFCD9B7F)
val TimelineLine = Color(0xFFE8C4B4)

// Border Colors
val GreenBorder = Color(0xFF4CAF50)
val OrangeBorder = Color(0xFFD4845D)
val RedBorder = Color(0xFFE74C3C)
val GreyBorder = Color(0xFFE0E0E0)
val GoogleButtonOutline = Color(0xFFE0E0E0)

// Status Colors
val ErrorRed = Color(0xFFE74C3C)
val RedLocation = Color(0xFFE74C3C)
val BlueAccent = Color(0xFF5A8FB8)
val StarColor = Color(0xFFFDB947)
val GreenHealthy = Color(0xFF34C759)

// Bubble Colors (Chat)
val UserBubbleColor = Color(0xFFD4845D)
val AIBubbleColor = Color(0xFFF5F5F5)
val ConsultationCardBackground = Color(0xFFFFF9F0)

// Dark Theme Colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkOnBackground = Color(0xFFE0E0E0)
val DarkOnSurface = Color(0xFFE0E0E0)
val White = Color(0xFFFFFFFF)