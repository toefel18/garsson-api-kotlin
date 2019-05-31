package nl.toefel.garsson

//import mu.NamedKLogging
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.server.GarssonRouter
import org.jetbrains.exposed.sql.Database
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

//    transaction {
//        println(SchemaUtils.createStatements(ProductsTable))
//    }
//
//    System.exit(0)

    val auth = JwtHmacAuthenticator(config.jwtSigningSecret, config.tokenValidity)

    logger.info("Starting API server")
    val server = GarssonRouter(config, auth)
    server.start()
    Runtime.getRuntime().addShutdownHook(Thread(Runnable { server.stop() }))
}
