package nl.toefel.garsson

import nl.toefel.garsson.util.now
import java.time.Duration

data class Config(
    val startedTime: String = now(),
    val port: Int,
    val jwtSigningSecret: String,
    val tokenValidity: Duration,
    val datasourceUser: String,
    val datasourcePassword: String,
    val datasourceUrl: String,
    val datasourceDriverClassName: String,
    val applicationName: String = MAVEN_NAME,
    val applicationVersion: String = VERSION,
    val applicationBuildDate: String = BUILD_DATE,
    val applicationGitHash: String = GIT_SHA,
    val applicationGitBranch: String = GIT_BRANCH,
    val applicationGitDate: String = GIT_DATE
    ) {

    fun safeForLogging() = this.copy(
        jwtSigningSecret = "*".repeat(jwtSigningSecret.length),
        datasourcePassword = "*".repeat(datasourcePassword.length))

    companion object {
        private const val DEFAULT_PORT = 8080
        private const val DEFAULT_JWT_SIGNING_SECRET = "jwt_secret_for_tests"
        private const val DEFAULT_TOKEN_VALIDITY_HOURS = 12
        private const val DEFAULT_DB_URL = "jdbc:postgresql://127.0.0.1/garsson-api"
        private const val DEFAULT_DB_USER = "garsson-api"
        private const val DEFAULT_DB_PASSWORD = "garsson-api"
        private const val DEFAULT_DB_DRIVER = "org.postgresql.Driver"

        fun fromEnvironment() = Config(
            port = System.getenv("PORT")?.toIntOrDefault(DEFAULT_PORT) ?: DEFAULT_PORT,
            jwtSigningSecret = System.getenv("JWT_SIGNING_SECRET") ?: DEFAULT_JWT_SIGNING_SECRET,
            tokenValidity = Duration.ofHours(System.getenv("TOKEN_VALIDITY_HOURS")?.toIntOrDefault(DEFAULT_TOKEN_VALIDITY_HOURS)?.toLong()
                ?: DEFAULT_TOKEN_VALIDITY_HOURS.toLong()),
            datasourceUser = System.getenv("DB_USER") ?: DEFAULT_DB_USER,
            datasourcePassword = System.getenv("DB_PASSWORD") ?: DEFAULT_DB_PASSWORD,
            datasourceUrl = System.getenv("DB_URL") ?: DEFAULT_DB_URL,
            datasourceDriverClassName = System.getenv("DB_DRIVER") ?: DEFAULT_DB_DRIVER
        )
    }
}

fun String.toIntOrDefault(default: Int): Int = try {
    this.toInt()
} catch (ex: NumberFormatException) {
    default
}