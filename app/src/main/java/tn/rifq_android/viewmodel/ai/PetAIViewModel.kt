package tn.rifq_android.viewmodel.ai

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tn.rifq_android.data.api.RetrofitInstance
import tn.rifq_android.data.model.ai.*
import tn.rifq_android.data.storage.TokenManager
import java.util.*

class PetAIViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val aiApi = RetrofitInstance.aiApi

    // Per-pet data for HomeScreen
    private val _petTips = MutableStateFlow<Map<String, List<PetTip>>>(emptyMap())
    val petTips: StateFlow<Map<String, List<PetTip>>> = _petTips.asStateFlow()

    private val _petStatuses = MutableStateFlow<Map<String, PetStatus>>(emptyMap())
    val petStatuses: StateFlow<Map<String, PetStatus>> = _petStatuses.asStateFlow()

    private val _petReminders = MutableStateFlow<Map<String, List<PetReminder>>>(emptyMap())
    val petReminders: StateFlow<Map<String, List<PetReminder>>> = _petReminders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Cache with timestamps (24 hour TTL)
    private val tipsCache = mutableMapOf<String, Pair<List<PetTip>, Long>>()
    private val statusCache = mutableMapOf<String, Pair<PetStatus, Long>>()
    private val remindersCache = mutableMapOf<String, Pair<List<PetReminder>, Long>>()
    private val cacheTTL = 24 * 60 * 60 * 1000L // 24 hours in milliseconds

    /**
     * Generate tips for a pet
     */
    fun generateTips(petId: String) {
        viewModelScope.launch {
            // Check cache first (outside try block for error handling)
            val cached = tipsCache[petId]
            if (cached != null && System.currentTimeMillis() - cached.second < cacheTTL) {
                Log.d("PetAIViewModel", "Using cached tips for pet $petId")
                updatePetTips(petId, cached.first)
                // Still try to refresh in background
            }
            
            try {
                _isLoading.value = true
                _error.value = null

                val response = aiApi.getTips(petId)
                
                val tips = response.tips.map { tip ->
                    PetTip(
                        id = UUID.randomUUID().toString(),
                        emoji = tip.emoji,
                        title = tip.title,
                        detail = tip.detail
                    )
                }

                // Update cache
                tipsCache[petId] = Pair(tips, System.currentTimeMillis())
                updatePetTips(petId, tips)

                Log.d("PetAIViewModel", "Generated ${tips.size} tips for pet $petId")
                    
            } catch (e: Exception) {
                Log.e("PetAIViewModel", "Error generating tips for pet $petId", e)
                // Show cached content even if new generation fails (iOS Reference: HomeView.swift lines 546-550)
                if (cached != null) {
                    Log.d("PetAIViewModel", "Using cached tips due to error")
                    updatePetTips(petId, cached.first)
                } else {
                    _error.value = "Failed to generate tips: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Generate status for a pet
     */
    fun generateStatus(petId: String) {
        viewModelScope.launch {
            try {
                // Check cache first
                val cached = statusCache[petId]
                if (cached != null && System.currentTimeMillis() - cached.second < cacheTTL) {
                    Log.d("PetAIViewModel", "Using cached status for pet $petId")
                    updatePetStatus(petId, cached.first)
                    return@launch
                }

                _isLoading.value = true
                _error.value = null

                val response = aiApi.getStatus(petId)
                
                val status = PetStatus(
                    status = response.status,
                    summary = response.summary,
                    pills = response.pills.map { pill ->
                        StatusPill(
                            text = pill.text,
                            backgroundColor = parseColor(pill.bg),
                            textColor = parseColor(pill.fg)
                        )
                    }
                )

                // Update cache
                statusCache[petId] = Pair(status, System.currentTimeMillis())
                updatePetStatus(petId, status)

                Log.d("PetAIViewModel", "Generated status for pet $petId: ${status.status}")
                
            } catch (e: Exception) {
                Log.e("PetAIViewModel", "Error generating status for pet $petId", e)
                // Use cached content if available (iOS Reference: HomeView.swift lines 546-550)
                val cached = statusCache[petId]
                if (cached != null) {
                    Log.d("PetAIViewModel", "Using cached status due to error")
                    updatePetStatus(petId, cached.first)
                } else {
                    _error.value = "Failed to generate status: ${e.message}"
                    // Fallback to default status
                    val fallback = PetStatus(
                        status = "Healthy",
                        summary = "All good",
                        pills = emptyList()
                    )
                    updatePetStatus(petId, fallback)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Generate reminders for a pet with calendar events merging
     * iOS Reference: PetAIViewModel.swift generateHomeReminders (lines 467-547)
     * Merges AI-generated reminders with calendar events
     */
    fun generateReminders(petId: String, petName: String = "", calendarEvents: List<tn.rifq_android.util.CalendarEvent> = emptyList()) {
        viewModelScope.launch {
            try {
                Log.d("PetAIViewModel", "🔔 generateReminders called for pet $petId")
                
                // Check cache first (for AI reminders only)
                val cached = remindersCache[petId]
                if (cached != null && System.currentTimeMillis() - cached.second < cacheTTL) {
                    Log.d("PetAIViewModel", "Using cached AI reminders for pet $petId")
                    // Still merge with fresh calendar events
                    val allReminders = mergeRemindersWithCalendar(cached.first, petName, calendarEvents)
                    updatePetReminders(petId, allReminders)
                    return@launch
                }

                _isLoading.value = true
                _error.value = null

                Log.d("PetAIViewModel", "🌐 Calling backend AI service for reminders...")
                val response = aiApi.getReminders(petId)
                Log.d("PetAIViewModel", "✅ Received reminders response from backend")
                Log.d("PetAIViewModel", "📋 Backend returned ${response.reminders.size} reminders")
                
                // Convert backend reminders to PetReminder
                val aiReminders = response.reminders.map { reminder ->
                    Log.d("PetAIViewModel", "   - Reminder: ${reminder.title} - ${reminder.detail.take(50)}")
                    
                    PetReminder(
                        id = UUID.randomUUID().toString(),
                        icon = reminder.icon,
                        title = reminder.title,
                        detail = reminder.detail,
                        date = reminder.date,
                        tint = parseColor(reminder.tint)
                    )
                }

                // Update cache with AI reminders
                remindersCache[petId] = Pair(aiReminders, System.currentTimeMillis())
                
                // Merge with calendar events (iOS Reference: PetAIViewModel.swift lines 500-516)
                val allReminders = mergeRemindersWithCalendar(aiReminders, petName, calendarEvents)
                
                updatePetReminders(petId, allReminders)

                Log.d("PetAIViewModel", "✅ Parsed ${allReminders.size} total reminders (${aiReminders.size} AI + ${allReminders.size - aiReminders.size} calendar)")
                
            } catch (e: Exception) {
                Log.e("PetAIViewModel", "Error generating reminders for pet $petId", e)
                
                // Try to use cached data even on error (iOS Reference: similar pattern)
                val cached = remindersCache[petId]
                if (cached != null) {
                    Log.d("PetAIViewModel", "Using cached reminders due to error")
                    val allReminders = mergeRemindersWithCalendar(cached.first, petName, calendarEvents)
                    updatePetReminders(petId, allReminders)
                } else {
                    _error.value = "Failed to generate reminders: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Merge AI reminders with calendar events
     * iOS Reference: PetAIViewModel.swift lines 500-516
     */
    private fun mergeRemindersWithCalendar(
        aiReminders: List<PetReminder>,
        petName: String,
        calendarEvents: List<tn.rifq_android.util.CalendarEvent>
    ): List<PetReminder> {
        val now = System.currentTimeMillis()
        
        // Convert future calendar events to reminders (take max 3)
        val calendarReminders = calendarEvents
            .filter { it.startTime >= now }
            .sortedBy { it.startTime }
            .take(3)
            .map { event ->
                PetReminder(
                    id = UUID.randomUUID().toString(),
                    icon = "📅", // Calendar icon
                    title = if (petName.isNotEmpty()) "$petName • ${event.title}" else event.title,
                    detail = event.description.ifEmpty { "Calendar Event" },
                    date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(Date(event.startTime)),
                    tint = Color(0xFFFF6B35) // Orange color for calendar events
                )
            }
        
        // Merge and sort by date
        return (aiReminders + calendarReminders).sortedBy { reminder ->
            try {
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(reminder.date)?.time ?: Long.MAX_VALUE
            } catch (e: Exception) {
                Long.MAX_VALUE
            }
        }
    }

    /**
     * Generate all AI content for a pet (tips, status, reminders)
     */
    fun generateAllContent(petId: String, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _isLoading.value = true
            }
            
            try {
                generateTips(petId)
                generateStatus(petId)
                generateReminders(petId)
            } catch (e: Exception) {
                Log.e("PetAIViewModel", "Error generating all content for pet $petId", e)
                if (!silent) {
                    _error.value = "Failed to generate AI content: ${e.message}"
                }
            } finally {
                if (!silent) {
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Generate content for multiple pets with calendar integration
     * iOS Reference: HomeView.swift lines 437-503
     * Updates UI immediately after each pet's content is generated
     */
    fun generateContentForPets(
        pets: List<Pair<String, String>>, // List of (petId, petName)
        calendarEventsMap: Map<String, List<tn.rifq_android.util.CalendarEvent>> = emptyMap(),
        silent: Boolean = true
    ) {
        viewModelScope.launch {
            // Process each pet one by one for progressive loading
            pets.forEachIndexed { index, (petId, petName) ->
                try {
                    // Generate tips first (updates UI immediately)
                    generateTips(petId)
                    
                    // Generate status (updates UI immediately)
                    generateStatus(petId)
                    
                    // Generate reminders with calendar events (updates UI immediately)
                    val calendarEvents = calendarEventsMap[petId] ?: emptyList()
                    generateReminders(petId, petName, calendarEvents)
                    
                    Log.d("PetAIViewModel", "✅ Processed pet ${index + 1}/${pets.size}: $petId")
                } catch (e: Exception) {
                    Log.e("PetAIViewModel", "⚠️ Failed to process pet $petId", e)
                    // Continue with next pet even if this one fails
                }
            }
        }
    }
    
    /**
     * Legacy method - kept for backward compatibility
     */
    fun generateContentForPets(petIds: List<String>, silent: Boolean = true) {
        viewModelScope.launch {
            // Process each pet one by one for progressive loading
            petIds.forEachIndexed { index, petId ->
                try {
                    // Generate tips first (updates UI immediately)
                    generateTips(petId)
                    
                    // Generate status (updates UI immediately)
                    generateStatus(petId)
                    
                    // Generate reminders (updates UI immediately)
                    generateReminders(petId)
                    
                    Log.d("PetAIViewModel", "✅ Processed pet ${index + 1}/${petIds.size}: $petId")
                } catch (e: Exception) {
                    Log.e("PetAIViewModel", "⚠️ Failed to process pet $petId", e)
                    // Continue with next pet even if this one fails
                }
            }
        }
    }

    /**
     * Clear cache for a specific pet
     */
    fun clearCache(petId: String) {
        tipsCache.remove(petId)
        statusCache.remove(petId)
        remindersCache.remove(petId)
    }

    /**
     * Clear all cache
     */
    fun clearAllCache() {
        tipsCache.clear()
        statusCache.clear()
        remindersCache.clear()
    }

    // Helper methods to update state
    private fun updatePetTips(petId: String, tips: List<PetTip>) {
        _petTips.value = _petTips.value.toMutableMap().apply {
            this[petId] = tips
        }
    }

    private fun updatePetStatus(petId: String, status: PetStatus) {
        _petStatuses.value = _petStatuses.value.toMutableMap().apply {
            this[petId] = status
        }
    }

    private fun updatePetReminders(petId: String, reminders: List<PetReminder>) {
        _petReminders.value = _petReminders.value.toMutableMap().apply {
            this[petId] = reminders
        }
    }

    // Parse color from hex string
    private fun parseColor(colorString: String): Color {
        return try {
            val cleanColor = colorString.removePrefix("#")
            val colorInt = cleanColor.toLong(16)
            Color(colorInt)
        } catch (e: Exception) {
            Log.e("PetAIViewModel", "Error parsing color: $colorString", e)
            Color.Gray
        }
    }
}
