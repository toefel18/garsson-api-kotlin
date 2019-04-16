package nl.toefel.garsson

import nl.toefel.garsson.util.now
import java.time.Duration

data class Config(
        val startedTime: String = now(),
        val port: Int,
        val jwtSigningSecret: String,
        val tokenValidity: Duration) {

    fun safeForLogging() = this.copy(jwtSigningSecret = "*".repeat(jwtSigningSecret.length))

    companion object {
        private const val DEFAULT_PORT = 8080
        private const val DEFAULT_JWT_SIGNING_SECRET = "jwt_secret_for_test"
        private const val DEFAULT_TOKEN_VALIDITY_HOURS = 12

        fun fromEnvironment() = Config(
                port = System.getenv("PORT")?.toIntOrDefault(DEFAULT_PORT) ?: DEFAULT_PORT,
                jwtSigningSecret = System.getenv("JWT_SIGNING_SECRET") ?: DEFAULT_JWT_SIGNING_SECRET,
                tokenValidity = Duration.ofHours(System.getenv("TOKEN_VALIDITY_HOURS")?.toIntOrDefault(DEFAULT_TOKEN_VALIDITY_HOURS)?.toLong()
                        ?: DEFAULT_TOKEN_VALIDITY_HOURS.toLong())
        )
    }
}

fun String.toIntOrDefault(default: Int): Int = try {
    this.toInt()
} catch (ex: NumberFormatException) {
    default
}