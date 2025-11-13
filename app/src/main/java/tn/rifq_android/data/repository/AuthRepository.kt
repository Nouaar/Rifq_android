package tn.rifq_android.data.repository

import tn.rifq_android.data.api.AuthApi
import tn.rifq_android.data.model.auth.AuthResponse
import tn.rifq_android.data.model.auth.LoginRequest
import tn.rifq_android.data.model.auth.RegisterRequest
import tn.rifq_android.data.model.auth.VerifyEmailRequest
import retrofit2.Response

class AuthRepository(private val api: AuthApi) {

    suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return api.register(request)
    }

    suspend fun verifyEmail(request: VerifyEmailRequest): Response<AuthResponse> {
        return api.verifyEmail(request)
    }

    suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return api.login(request)
    }
}
