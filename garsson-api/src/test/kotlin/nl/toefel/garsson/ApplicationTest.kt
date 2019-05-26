package nl.toefel.garsson

import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.repository.UserEntity
import nl.toefel.garsson.repository.UsersTable
import nl.toefel.garsson.server.GarssonApiServer
import org.h2.jdbcx.JdbcDataSource
import org.h2.tools.RunScript
import org.h2.tools.Server
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.net.ServerSocket
import org.h2.tools.Server.createWebServer
import java.io.FileReader


object ApplicationTest : TestListener {

    val logger = LoggerFactory.getLogger("boot")
    var server: GarssonApiServer? = null
    val dbPort = getAvailablePort().toString()
    val db = Server.createTcpServer("-tcpPort", dbPort, "-tcpAllowOthers")
    val config = Config.fromEnvironment().copy(
        port = getAvailablePort(),
        datasourceUrl = "jdbc:h2:tcp://127.0.0.1:$dbPort",
        datasourceDriverClassName = "org.h2.Driver",
        datasourceUser = "sa",
        datasourcePassword = "sa"
        )

    override fun beforeTest(description: Description) {
        server?.stop()
        db.start()
        val ds = createHikariDataSource(config)

        migrate(ds)
        Database.Companion.connect(ds)

        transaction {
            UserEntity.all().forEach { println("JAAAAAAAAAAAAAAAAAA" + it.email) }
        }

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
        db.stop()
    }

    fun getAvailablePort(): Int = ServerSocket(0).use { socket -> return socket.localPort }
}