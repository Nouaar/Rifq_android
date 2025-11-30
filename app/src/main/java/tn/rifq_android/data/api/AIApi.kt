package tn.rifq_android.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.DELETE
import tn.rifq_android.data.model.ai.AIRemindersResponse
import tn.rifq_android.data.model.ai.AIStatusResponse
import tn.rifq_android.data.model.ai.AITipsResponse
import tn.rifq_android.data.model.ai.AIResponseModel
import tn.rifq_android.data.model.ai.ChatbotMessageRequest
import tn.rifq_android.data.model.ai.ChatbotResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.Part

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
     * Backend: POST /chatbot/message
     * iOS Reference: GeminiService.swift generateText
     */
    @POST("chatbot/message")
    suspend fun generateAIResponse(
        @Body request: ChatbotMessageRequest
    ): ChatbotResponse

    /**
     * Multipart variant: message + optional context + optional image file
     * Backend: POST /chatbot/message (multipart/form-data)
     */
    @Multipart
    @POST("chatbot/message")
    suspend fun generateAIResponseMultipart(
        @Part("message") message: RequestBody,
        @Part("context") context: RequestBody?,
        @Part image: MultipartBody.Part?
    ): ChatbotResponse

    /**
     * Get conversation history for current user
     * Backend: GET /chatbot/history
     */
    @GET("chatbot/history")
    suspend fun getHistory(
        @retrofit2.http.Query("limit") limit: Int? = null,
        @retrofit2.http.Query("offset") offset: Int? = null
    ): tn.rifq_android.data.model.ai.ChatbotHistoryResponse

    /**
     * Delete conversation history for current user
     * Backend: DELETE /chatbot/history
     */
    @DELETE("chatbot/history")
    suspend fun deleteHistory(): Response<tn.rifq_android.data.model.ai.ChatbotDeleteResponse>

    /**
     * Analyze image provided as base64 string and return analysis text
     * Backend: POST /chatbot/analyze-image-base64
     */
    @POST("chatbot/analyze-image-base64")
    suspend fun analyzeImageBase64(
        @Body request: tn.rifq_android.data.model.ai.ChatbotImageAnalysisRequest
    ): ChatbotResponse
}