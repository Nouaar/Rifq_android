package tn.rifq_android.data.model.pet

import com.squareup.moshi.Json

data class Pet(
    @Json(name = "_id") val id: String? = null,
    val name: String,
    val species: String, // e.g., 'dog', 'cat'
    val breed: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val color: String? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val photo: String? = null,
    val microchipId: String? = null
    // Note: owner and medicalHistory fields removed - backend returns them as objects
    // medicalHistory can be fetched separately via GET /pets/{petId}/medical-history when needed
)