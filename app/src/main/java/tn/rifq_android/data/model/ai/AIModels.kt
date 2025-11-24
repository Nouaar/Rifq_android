package tn.rifq_android.data.model.ai

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AITipsResponse(
    val tips: List<AITip>
)

@JsonClass(generateAdapter = true)
data class AITip(
    val emoji: String,
    val title: String,
    val detail: String
)

@JsonClass(generateAdapter = true)
data class AIRemindersResponse(
    val reminders: List<AIReminder>
)

@JsonClass(generateAdapter = true)
data class AIReminder(
    val icon: String,
    val title: String,
    val detail: String,
    val date: String, // ISO date string
    val tint: String // color hex string
)

@JsonClass(generateAdapter = true)
data class AIStatusResponse(
    val status: String,
    val summary: String,
    val pills: List<AIStatusPill>
)

@JsonClass(generateAdapter = true)
data class AIStatusPill(
    val text: String,
    val bg: String, // background color hex
    val fg: String  // foreground color hex
)

/**
 * AI Chat Response Model
 * iOS Reference: GeminiService.swift response parsing
 */
@JsonClass(generateAdapter = true)
data class AIResponseModel(
    val text: String?,
    val error: String? = null,
    val conversationId: String? = null
)

// UI models for display
data class PetTip(
    val id: String,
    val emoji: String,
    val title: String,
    val detail: String
)

data class PetReminder(
    val id: String,
    val icon: String,
    val title: String,
    val detail: String,
    val date: String,
    val tint: androidx.compose.ui.graphics.Color
)

data class PetStatus(
    val status: String,
    val pills: List<StatusPill>,
    val summary: String
)

data class StatusPill(
    val text: String,
    val backgroundColor: androidx.compose.ui.graphics.Color,
    val textColor: androidx.compose.ui.graphics.Color
)
