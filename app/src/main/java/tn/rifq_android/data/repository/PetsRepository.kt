package tn.rifq_android.data.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import tn.rifq_android.data.api.PetsApi
import java.io.File

class PetsRepository(private val api: PetsApi) {

    suspend fun getPetsByOwner(ownerId: String) = api.getPetsByOwner(ownerId)

    suspend fun getPetById(petId: String) = api.getPetById(petId)

    suspend fun addPet(
        ownerId: String,
        name: String,
        species: String,
        breed: String? = null,
        age: Int? = null,
        gender: String? = null,
        color: String? = null,
        weight: Double? = null,
        height: Double? = null,
        microchipId: String? = null,
        photoFile: File? = null
    ) = api.addPet(
        ownerId = ownerId,
        name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
        species = species.toRequestBody("text/plain".toMediaTypeOrNull()),
        breed = breed?.toRequestBody("text/plain".toMediaTypeOrNull()),
        age = age?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        gender = gender?.toRequestBody("text/plain".toMediaTypeOrNull()),
        color = color?.toRequestBody("text/plain".toMediaTypeOrNull()),
        weight = weight?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        height = height?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        microchipId = microchipId?.toRequestBody("text/plain".toMediaTypeOrNull()),
        photo = photoFile?.let {
            val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("photo", it.name, requestBody)
        }
    )

    suspend fun updatePet(
        petId: String,
        name: String? = null,
        species: String? = null,
        breed: String? = null,
        age: Int? = null,
        gender: String? = null,
        color: String? = null,
        weight: Double? = null,
        height: Double? = null,
        microchipId: String? = null,
        photoFile: File? = null
    ) = api.updatePet(
        petId = petId,
        name = name?.toRequestBody("text/plain".toMediaTypeOrNull()),
        species = species?.toRequestBody("text/plain".toMediaTypeOrNull()),
        breed = breed?.toRequestBody("text/plain".toMediaTypeOrNull()),
        age = age?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        gender = gender?.toRequestBody("text/plain".toMediaTypeOrNull()),
        color = color?.toRequestBody("text/plain".toMediaTypeOrNull()),
        weight = weight?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        height = height?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        microchipId = microchipId?.toRequestBody("text/plain".toMediaTypeOrNull()),
        photo = photoFile?.let {
            val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("photo", it.name, requestBody)
        }
    )

    suspend fun deletePet(ownerId: String, petId: String) = api.deletePet(ownerId, petId)
}

