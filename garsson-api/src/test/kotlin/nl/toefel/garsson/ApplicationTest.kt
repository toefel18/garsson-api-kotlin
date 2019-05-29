package nl.toefel.garsson

import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.server.GarssonApiServer
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import java.net.ServerSocket

/**
 * Before the tests it:
 * 1. Starts a postgresql container and migrates the schema (requires Docker!)
 * 2. starts the complete application and connects it to the postgresql container
 *
 * After the test it:
 * 1. shuts down the server
 * 2. shuts down postgresql
 */
object ApplicationTest : TestListener {

    val logger = LoggerFactory.getLogger("boot")

    var postgres: KGenericContainer? = null
    var config: Config? = null
    var server: GarssonApiServer? = null

    override fun beforeTest(description: Description) {
        server?.stop()
        postgres?.stop()

        logger.info("Starting postgres in test")

        postgres = KGenericContainer("postgres")
            .withEnv("POSTGRES_USER", "garsson-api")
            .withEnv("POSTGRES_PASSWORD", "garsson-api")
            .withExposedPorts(5432)

        postgres!!.start()

        config = Config.fromEnvironment().copy(
            port = getAvailablePort(),
            datasourceUrl = "jdbc:postgresql://127.0.0.1:${postgres!!.getMappedPort(5432)}/garsson-api")

        logger.info("Config: $config")

        logger.info("Creating data source")

        val ds = createHikariDataSource(config!!)

        logger.info("Migrating schema")
        migrate(ds)

        logger.info("Preparing connection for the application to use")
        Database.connect(ds)

        logger.info("Starting application for test")
        val auth = JwtHmacAuthenticator(config!!.jwtSigningSecret, config!!.tokenValidity)
        server = GarssonApiServer(config!!, auth)
        server?.start()

        logger.info("Started!")
        Runtime.getRuntime().addShutdownHook(Thread(Runnable { server?.stop() }))
        Runtime.getRuntime().addShutdownHook(Thread(Runnable { postgres?.stop() }))
    }

    override fun afterTest(description: Description, result: TestResult) {
        logger.info("Stopping application")
        server?.stop()
        logger.info("Stopping postgres")
        postgres?.stop()
    }

    fun getAvailablePort(): Int = ServerSocket(0).use { socket -> return socket.localPort }
}