package tn.rifq_android.data.model.auth

import com.squareup.moshi.Json

/**
 * App user model matching backend structure
 * Used across vet, sitter, and general user features
 */
data class AppUser(
    @Json(name = "_id") val id: String,
    val email: String,
    val name: String? = null,
    val role: String? = null,
    val avatarUrl: String? = null,
    val city: String? = null,
    val country: String? = null,
    val phone: String? = null,
    
    // Vet-specific fields
    val vetClinicName: String? = null,
    val vetAddress: String? = null,
    val vetSpecializations: List<String>? = null,
    val vetLicenseNumber: String? = null,
    val vetYearsOfExperience: Int? = null,
    val vetEmergencyAvailable: Boolean? = null,
    val vetBio: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // Sitter-specific fields
    val sitterAddress: String? = null,
    val hourlyRate: Double? = null,
    val services: List<String>? = null,
    val yearsOfExperience: Int? = null,
    val availableWeekends: Boolean? = null,
    val canHostPets: Boolean? = null,
    val availability: List<String>? = null,
    val bio: String? = null
)
