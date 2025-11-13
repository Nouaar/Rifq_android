package tn.rifq_android.data.model.auth

import com.squareup.moshi.Json

data class User(
    @Json(name = "_id") val id: String? = null,
    val name: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val profileImage: String? = null,
    val balance: Double? = null,
    val isVerified: Boolean? = null
)