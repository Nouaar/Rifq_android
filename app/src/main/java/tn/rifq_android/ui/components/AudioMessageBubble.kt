package tn.rifq_android.ui.components

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tn.rifq_android.ui.theme.*

/**
 * Audio Message Bubble Component
 * iOS Reference: AudioMessageBubble.swift
 * 
 * Displays an audio message with play/pause controls and waveform visualization
 */
@Composable
fun AudioMessageBubble(
    audioURL: String,
    isFromCurrentUser: Boolean,
    duration: Int? = null
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(0) }
    var totalDuration by remember { mutableStateOf(duration ?: 0) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val waveformHeights = remember { List(30) { (8..32).random().toFloat() } }
    
    // Animated wave heights when playing
    val animatedHeights = waveformHeights.map { baseHeight ->
        val infiniteTransition = rememberInfiniteTransition(label = "wave")
        val height by infiniteTransition.animateFloat(
            initialValue = baseHeight * 0.5f,
            targetValue = baseHeight,
            animationSpec = infiniteRepeatable(
                animation = tween(300, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "waveHeight"
        )
        if (isPlaying) height else baseHeight * 0.7f
    }
    
    // Update timer while playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(100)
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    currentTime = player.currentPosition / 1000
                }
            }
        }
    }
    
    // Initialize media player
    LaunchedEffect(audioURL) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioURL)
                prepareAsync()
                setOnPreparedListener { mp ->
                    totalDuration = mp.duration / 1000
                }
                setOnCompletionListener {
                    isPlaying = false
                    currentTime = 0
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioMessageBubble", "Error loading audio: ${e.message}")
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
    
    fun togglePlayback() {
        mediaPlayer?.let { player ->
            if (isPlaying) {
                player.pause()
                isPlaying = false
            } else {
                if (currentTime >= totalDuration) {
                    player.seekTo(0)
                    currentTime = 0
                }
                player.start()
                isPlaying = true
            }
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromCurrentUser) {
            // Other person's audio on the left
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardBackground,
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .border(0.5.dp, VetStroke.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                    .clickable { togglePlayback() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(OrangeAccent.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Playing" else "Play",
                            tint = OrangeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    // Waveform
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        animatedHeights.take(25).forEach { height ->
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(height.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(OrangeAccent.copy(alpha = 0.7f))
                            )
                        }
                    }
                    
                    // Duration
                    Text(
                        text = formatTime(if (isPlaying) currentTime else totalDuration),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.widthIn(min = 45.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(50.dp))
        } else {
            // My audio on the right
            Spacer(modifier = Modifier.width(50.dp))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = OrangeAccent,
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clickable { togglePlayback() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Playing" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    // Waveform
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        animatedHeights.take(25).forEach { height ->
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(height.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(Color.White.copy(alpha = 0.7f))
                            )
                        }
                    }
                    
                    // Duration
                    Text(
                        text = formatTime(if (isPlaying) currentTime else totalDuration),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.widthIn(min = 45.dp)
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}
