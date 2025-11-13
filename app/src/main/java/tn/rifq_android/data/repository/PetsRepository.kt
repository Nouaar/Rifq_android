package tn.rifq_android.data.repository

import tn.rifq_android.data.api.PetsApi
import tn.rifq_android.data.model.pet.AddPetRequest
import tn.rifq_android.data.model.pet.UpdatePetRequest

class PetsRepository(private val api: PetsApi) {

    suspend fun getPetsByOwner(ownerId: String) = api.getPetsByOwner(ownerId)

    suspend fun getPetById(petId: String) = api.getPetById(petId)

    suspend fun addPet(ownerId: String, request: AddPetRequest) = api.addPet(ownerId, request)

    suspend fun updatePet(petId: String, request: UpdatePetRequest) = api.updatePet(petId, request)

    suspend fun deletePet(ownerId: String, petId: String) = api.deletePet(ownerId, petId)
}

