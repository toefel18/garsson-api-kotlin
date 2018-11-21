package nl.toefel.garsson

data class Config(
        val port: Int,
        val jwtSigningSecret: String) {

    fun safeForLogging() = this.copy(jwtSigningSecret = "*".repeat(jwtSigningSecret.length))

    companion object {
        fun fromEnvironment() = Config(
            port = System.getenv("PORT")?.toIntOrDefault(8080) ?: 8080,
            jwtSigningSecret = System.getenv("JWT_SIGNING_SECRET") ?: "jwt_secret_for_test"
        )
    }
}

fun String.toIntOrDefault(default: Int): Int = try {
    this.toInt()
} catch (ex: NumberFormatException) {
    default
}