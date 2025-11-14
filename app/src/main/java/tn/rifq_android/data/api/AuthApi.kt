package tn.rifq_android.data.api

import tn.rifq_android.data.model.auth.AuthResponse
import tn.rifq_android.data.model.auth.LoginRequest
import tn.rifq_android.data.model.auth.RegisterRequest
import tn.rifq_android.data.model.auth.VerifyEmailRequest
import tn.rifq_android.data.model.auth.RefreshRequest
import tn.rifq_android.data.model.auth.ForgotPasswordRequest
import tn.rifq_android.data.model.auth.ResetPasswordRequest
import tn.rifq_android.data.model.auth.ChangePasswordRequest
import tn.rifq_android.data.model.auth.ChangeEmailRequest
import tn.rifq_android.data.model.auth.VerifyNewEmailRequest
import tn.rifq_android.data.model.auth.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import tn.rifq_android.data.model.auth.GoogleSignInRequest

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/verify")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // Refresh access token using refresh token
    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<AuthResponse>

    // Password Management
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>

    @PATCH("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    // Email Management
    @PATCH("auth/change-email")
    suspend fun changeEmail(@Body request: ChangeEmailRequest): Response<MessageResponse>

    @POST("auth/verify-new-email")
    suspend fun verifyNewEmail(@Body request: VerifyNewEmailRequest): Response<MessageResponse>

    // Google Sign-In
    @POST("auth/google")
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): Response<AuthResponse>
}
