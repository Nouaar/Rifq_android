package tn.rifq_android.viewmodel.ai

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.rifq_android.data.api.RetrofitInstance
import tn.rifq_android.ui.screens.ai.AIChatMessage

/**
 * ViewModel for AI Chat Assistant
 * iOS Reference: PetAIViewModel.swift + ChatViewModel
 * Manages conversational AI interactions
 */
class AIChatViewModel(private val context: Context) : ViewModel() {

    private val aiApi = RetrofitInstance.aiApi

    private val _messages = MutableStateFlow<List<AIChatMessage>>(emptyList())
    val messages: StateFlow<List<AIChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        // Add welcome message
        _messages.value = listOf(
            AIChatMessage(
                content = "👋 Hi! I'm your AI vet assistant. I can help answer questions about your pet's health, nutrition, behavior, and general care. How can I help you today?",
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    /**
     * Send message to AI and get response
     * iOS Reference: GeminiService.swift generateText
     */
    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                
                // Add user message
                val userMsg = AIChatMessage(
                    content = userMessage,
                    isFromUser = true
                )
                _messages.value = _messages.value + userMsg
                
                _isLoading.value = true
                
                // Build conversation context (last 10 messages for context)
                val conversationHistory = _messages.value
                    .takeLast(10)
                    .joinToString("\n") { msg ->
                        if (msg.isFromUser) "User: ${msg.content}" else "Assistant: ${msg.content}"
                    }
                
                // Build enriched prompt with pet context
                val enrichedPrompt = buildContextualPrompt(userMessage, conversationHistory)
                
                // Call AI API (backend endpoint)
                val response = aiApi.generateAIResponse(
                    mapOf(
                        "prompt" to enrichedPrompt,
                        "conversationHistory" to conversationHistory
                    )
                )
                
                // Add AI response
                val aiMsg = AIChatMessage(
                    content = response.text ?: "I'm sorry, I couldn't generate a response. Please try again.",
                    isFromUser = false
                )
                _messages.value = _messages.value + aiMsg
                
            } catch (e: Exception) {
                _error.value = "Failed to get AI response: ${e.message}"
                android.util.Log.e("AIChatViewModel", "AI Error: ${e.message}", e)
                
                // Add fallback message
                val fallbackMsg = AIChatMessage(
                    content = "I'm sorry, I'm having trouble connecting right now. Please try again later or consult your veterinarian for urgent matters.",
                    isFromUser = false
                )
                _messages.value = _messages.value + fallbackMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Build contextual prompt with pet information
     * iOS Reference: GeminiService.swift prompt building
     */
    private fun buildContextualPrompt(userMessage: String, conversationHistory: String): String {
        return """
            You are a professional veterinary AI assistant. Your role is to provide helpful, accurate, and caring advice about pet health and care.
            
            Guidelines:
            - Be empathetic and professional
            - Provide clear, actionable advice
            - For serious symptoms, recommend consulting a veterinarian
            - Use simple language
            - Be concise but thorough
            
            Conversation History:
            $conversationHistory
            
            User's Current Question: $userMessage
            
            Please provide a helpful response:
        """.trimIndent()
    }

    /**
     * Clear conversation history
     */
    fun clearChat() {
        _messages.value = listOf(
            AIChatMessage(
                content = "Chat cleared. How can I help you today?",
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
        _error.value = null
    }

    /**
     * Get quick response for specific pet health topics
     */
    fun getQuickAdvice(topic: String) {
        val quickPrompts = mapOf(
            "vaccines" to "What vaccines does a typical dog/cat need and when?",
            "nutrition" to "What are the nutritional requirements for a healthy pet?",
            "symptoms" to "What are common pet health symptoms I should watch for?",
            "emergencies" to "What are veterinary emergencies that require immediate attention?",
            "grooming" to "How often should I groom my pet and what does it involve?"
        )
        
        quickPrompts[topic]?.let { prompt ->
            sendMessage(prompt)
        }
    }
}

