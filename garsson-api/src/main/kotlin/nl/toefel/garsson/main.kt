package nl.toefel.garsson

//import mu.NamedKLogging
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.repository.UserEntity
import nl.toefel.garsson.repository.UsersTable
import nl.toefel.garsson.server.GarssonApiServer
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("boot")
    val config = Config.fromEnvironment()
    logger.info("Starting, config:  ${config.safeForLogging()}")

    logger.info("Creating data source")
    val ds = createHikariDataSource(config)

    logger.info("Migrating database")
    migrate(ds)

    // create connection which can be used via
    // transaction {} in code
    Database.connect(ds)

    val auth = JwtHmacAuthenticator(config.jwtSigningSecret, config.tokenValidity)

    logger.info("Starting API server")
    val server = GarssonApiServer(config, auth)
    server.start()
    Runtime.getRuntime().addShutdownHook(Thread(Runnable { server.stop() }))
    logger.info("Started.")
}
