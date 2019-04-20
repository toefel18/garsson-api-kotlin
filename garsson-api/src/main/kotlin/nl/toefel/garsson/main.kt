package nl.toefel.garsson

//import mu.NamedKLogging
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.server.GarssonApiServer
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
//    val logger = object : NamedKLogging("boot"){}.logger
    val logger = LoggerFactory.getLogger("boot")
    val config = Config.fromEnvironment()
    logger.info("Starting, config:  $config")

    val auth = JwtHmacAuthenticator(config.jwtSigningSecret, config.tokenValidity)
//    println(auth.generateJwt(User("Toefel", listOf("admin", "user"))))

    val server = GarssonApiServer(config, auth)
    server.start()
    Runtime.getRuntime().addShutdownHook(Thread(Runnable { server.stop() }))
}