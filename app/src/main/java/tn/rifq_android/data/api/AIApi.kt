package tn.rifq_android.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import tn.rifq_android.data.model.ai.AIRemindersResponse
import tn.rifq_android.data.model.ai.AIStatusResponse
import tn.rifq_android.data.model.ai.AITipsResponse
import tn.rifq_android.data.model.ai.AIResponseModel

/**
 * AI API endpoints
 * iOS Reference: GeminiService.swift
 */
interface AIApi {
    
    @GET("ai/pets/{petId}/tips")
    suspend fun getTips(
        @Path("petId") petId: String
    ): AITipsResponse
    
    @GET("ai/pets/{petId}/reminders")
    suspend fun getReminders(
        @Path("petId") petId: String
    ): AIRemindersResponse
    
    @GET("ai/pets/{petId}/status")
    suspend fun getStatus(
        @Path("petId") petId: String
    ): AIStatusResponse
    
    /**
     * Conversational AI chat endpoint
     * iOS Reference: GeminiService.swift generateText
     */
    @POST("ai/chat")
    suspend fun generateAIResponse(
        @Body request: Map<String, String>
    ): AIResponseModel
}
