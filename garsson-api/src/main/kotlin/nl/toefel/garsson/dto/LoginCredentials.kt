package nl.toefel.garsson.dto

data class LoginCredentials(val email: String, val password: String)

data class SuccessfulLoginResponse(val token: String)