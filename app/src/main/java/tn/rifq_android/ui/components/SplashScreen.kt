package tn.rifq_android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tn.rifq_android.ui.theme.VetCanyon
import java.util.*

/**
 * Splash Screen matching iOS SplashView design
 * iOS Reference: SplashView.swift
 */
@Composable
fun SplashScreen(
    onAnimationComplete: () -> Unit = {}
) {
    // Animation states
    var pawWave by remember { mutableStateOf(false) }
    var titleScale by remember { mutableStateOf(0.92f) }
    var titleOpacity by remember { mutableStateOf(0f) }
    var subtitleOpacity by remember { mutableStateOf(0f) }
    var dotsIndex by remember { mutableStateOf(0) }
    var animationDone by remember { mutableStateOf(false) }

    // Animation durations (matching iOS)
    val appearDelay = 100L
    val totalDuration = 2100L
    val dotsInterval = 550L

    // Launch animations
    LaunchedEffect(Unit) {
        // Start paw wave animation
        pawWave = true

        // Delay for title/subtitle animations
        delay(appearDelay)
        titleScale = 1.0f
        titleOpacity = 1.0f

        delay(appearDelay + 160)
        subtitleOpacity = 1.0f

        // Complete animation after total duration
        delay(totalDuration - appearDelay - 160)
        animationDone = true
        onAnimationComplete()
    }

    // Dots animation timer
    LaunchedEffect(Unit) {
        while (!animationDone) {
            delay(dotsInterval)
            dotsIndex = (dotsIndex + 1) % 3
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VetCanyon),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = (-8).dp)
        ) {
            // Two paw prints with wave animation
            Box(
                modifier = Modifier.padding(bottom = 26.dp)
            ) {
                Paw(
                    angle = -18f,
                    baseX = (-12).dp,
                    baseY = (-8).dp,
                    wave = pawWave,
                    delay = 0L
                )
                Paw(
                    angle = 18f,
                    baseX = 24.dp,
                    baseY = 18.dp,
                    wave = pawWave,
                    delay = 120L
                )
            }

            // Title "RifQ" with scale and fade animation
            Text(
                text = "RifQ",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.4.sp,
                modifier = Modifier
                    .scale(titleScale)
                    .alpha(titleOpacity)
            )

            // Subtitle "PET HEALTHCARE" with fade animation
            Text(
                text = "PET HEALTHCARE",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.92f),
                modifier = Modifier
                    .offset(y = (-6).dp)
                    .alpha(subtitleOpacity)
            )

            // Three animated dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 10.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (index == dotsIndex) {
                                    Color.White
                                } else {
                                    Color.White.copy(alpha = 0.45f)
                                },
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .scale(if (index == dotsIndex) 1.15f else 1.0f)
                    )
                }
            }
        }
    }
}

@Composable
private fun Paw(
    angle: Float,
    baseX: androidx.compose.ui.unit.Dp,
    baseY: androidx.compose.ui.unit.Dp,
    wave: Boolean,
    delay: Long
) {
    val waveOffset by animateFloatAsState(
        targetValue = if (wave) -3.5f else 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pawWave"
    )

    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = null,
        modifier = Modifier
            .size(58.dp)
            .rotate(angle)
            .offset(x = baseX, y = baseY + waveOffset.dp),
        tint = Color.Black.copy(alpha = 0.75f)
    )
}
