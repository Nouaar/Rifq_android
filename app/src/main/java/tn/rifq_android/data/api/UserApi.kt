package tn.rifq_android.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import tn.rifq_android.data.model.auth.User as AuthUser

interface UserApi {

    @GET("users/profile")
    suspend fun getProfile(): Response<AuthUser>

    @Multipart
    @PATCH("users/profile")
    suspend fun updateProfile(
        @Part("name") name: RequestBody? = null,
        @Part("email") email: RequestBody? = null,
        @Part photo: MultipartBody.Part? = null
    ): Response<AuthUser>

    @DELETE("users/profile")
    suspend fun deleteAccount(): Response<Unit>
}
