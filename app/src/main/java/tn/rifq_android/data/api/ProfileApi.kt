package tn.rifq_android.data.api

import retrofit2.Response
import retrofit2.http.*
import tn.rifq_android.data.model.*

interface ProfileApi {

    @GET("users/{id}")
    suspend fun getProfile(@Path("id") userId: String): Response<User>

    @PUT("users/{id}")
    suspend fun updateProfile(
        @Path("id") userId: String,
        @Body request: UpdateProfileRequest
    ): Response<User>

    @GET("pets/owner/{ownerId}")
    suspend fun getPets(@Path("ownerId") ownerId: String): Response<List<Pet>>

    @POST("pets/owner/{ownerId}")
    suspend fun addPet(
        @Path("ownerId") ownerId: String,
        @Body request: AddPetRequest
    ): Response<Pet>

    @PUT("pets/{petId}")
    suspend fun updatePet(
        @Path("petId") petId: String,
        @Body request: UpdatePetRequest
    ): Response<Pet>

    @DELETE("pets/{ownerId}/{petId}")
    suspend fun deletePet(
        @Path("ownerId") ownerId: String,
        @Path("petId") petId: String
    ): Response<Unit>
}

