package tn.rifq_android.data.model

import com.squareup.moshi.Json

data class User(
    @Json(name = "_id") val id: String? = null,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val profileImage: String? = null,
    val balance: Double? = null,
    val isVerified: Boolean? = null,
    val pets: List<Pet>? = null
)

data class Pet(
    @Json(name = "_id") val id: String,
    val name: String,
    val type: String,
    val breed: String,
    val age: Int,
    val description: String? = null,
    val imageUrl: String? = null,
    val ownerId: String
)

data class ProfileResponse(
    val user: User,
    val pets: List<Pet>
)

data class UpdateProfileRequest(
    val name: String,
    val phone: String? = null
)

data class AddPetRequest(
    val name: String,
    val type: String,
    val breed: String,
    val age: Int,
    val description: String? = null
)

data class UpdatePetRequest(
    val name: String,
    val type: String,
    val breed: String,
    val age: Int,
    val description: String? = null
)

