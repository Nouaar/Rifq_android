package tn.rifq_android.model

data class AuthResponse(
    val message: String?,
    val tokens: Tokens? = null,
    val user: User? = null
)

data class Tokens(
    val accessToken: String,
    val refreshToken: String
)

