package tn.rifq_android.data.api

import tn.rifq_android.data.model.auth.AuthResponse
import tn.rifq_android.data.model.auth.LoginRequest
import tn.rifq_android.data.model.auth.RegisterRequest
import tn.rifq_android.data.model.auth.VerifyEmailRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/verify")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
