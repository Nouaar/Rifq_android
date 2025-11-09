package tn.rifq_android.data.repository

import retrofit2.Response
import tn.rifq_android.data.api.ProfileApi
import tn.rifq_android.model.*

class ProfileRepository(private val api: ProfileApi) {

    suspend fun getProfile(userId: String): Response<User> {
        return api.getProfile(userId)
    }

    suspend fun updateProfile(userId: String, request: UpdateProfileRequest): Response<User> {
        return api.updateProfile(userId, request)
    }

    suspend fun getPets(ownerId: String): Response<List<Pet>> {
        return api.getPets(ownerId)
    }

    suspend fun addPet(ownerId: String, request: AddPetRequest): Response<Pet> {
        return api.addPet(ownerId, request)
    }

    suspend fun updatePet(petId: String, request: UpdatePetRequest): Response<Pet> {
        return api.updatePet(petId, request)
    }

    suspend fun deletePet(ownerId: String, petId: String): Response<Unit> {
        return api.deletePet(ownerId, petId)
    }
}

