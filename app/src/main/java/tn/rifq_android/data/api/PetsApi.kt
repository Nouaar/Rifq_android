package tn.rifq_android.data.api

import retrofit2.Response
import retrofit2.http.*
import tn.rifq_android.data.model.*
import tn.rifq_android.data.model.pet.AddPetRequest
import tn.rifq_android.data.model.pet.Pet
import tn.rifq_android.data.model.pet.UpdatePetRequest

interface PetsApi {

    @GET("pets/owner/{ownerId}")
    suspend fun getPetsByOwner(@Path("ownerId") ownerId: String): Response<List<Pet>>

    @GET("pets/{petId}")
    suspend fun getPetById(@Path("petId") petId: String): Response<Pet>

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

