package nl.toefel.garsson

import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.server.GarssonApiServer
import org.slf4j.LoggerFactory
import java.net.ServerSocket

object ApplicationTest : TestListener {

    val logger = LoggerFactory.getLogger("boot")
    var server: GarssonApiServer? = null
    val config = Config.fromEnvironment().copy(port = getAvailablePort())

    override fun beforeTest(description: Description) {
        server?.stop()

        logger.info("Starting application for test")
        logger.info("Config: $config")

        val auth = JwtHmacAuthenticator(config.jwtSigningSecret, config.tokenValidity)
        server = GarssonApiServer(config, auth)
        server?.start()
        Runtime.getRuntime().addShutdownHook(Thread(Runnable { server?.stop() }))
    }

    override fun afterTest(description: Description, result: TestResult) {
        logger.info("Stopping application")
        server?.stop()
    }

    fun getAvailablePort(): Int = ServerSocket(0).use { socket -> return socket.localPort }
}