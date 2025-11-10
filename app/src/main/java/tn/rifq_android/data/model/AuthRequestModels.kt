package tn.rifq_android.data.model

enum class UserRole(val value: String) {
    OWNER("owner"),
    SITTER("sitter"),
    VET("vet")
}

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val role: String
)

data class VerifyEmailRequest(
    val email: String,
    val code: String
)

data class LoginRequest(
    val email: String,
    val password: String
)
