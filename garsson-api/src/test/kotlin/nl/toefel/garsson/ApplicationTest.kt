package nl.toefel.garsson

import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.server.GarssonRouter
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import org.testcontainers.containers.wait.strategy.Wait
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
    var server: GarssonRouter? = null

    override fun beforeTest(description: Description) {
        server?.stop()
        postgres?.stop()

        logger.info("Starting postgres in test")

        // TODO replace with https://github.com/opentable/otj-pg-embedded ??
        postgres = KGenericContainer("postgres")
            .withEnv("POSTGRES_USER", "garsson-api")
            .withEnv("POSTGRES_PASSWORD", "garsson-api")
            .withExposedPorts(5432)
            .waitingFor(Wait.forListeningPort())

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
        val db = Database.connect(ds)
        db.useNestedTransactions = true

        logger.info("Starting application for test")
        val auth = JwtHmacAuthenticator(config!!.jwtSigningSecret, config!!.tokenValidity)
        server = GarssonRouter(config!!, auth)
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