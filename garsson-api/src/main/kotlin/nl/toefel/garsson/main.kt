package nl.toefel.garsson

import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.auth.User
import nl.toefel.garsson.server.GarssonApiServer
import java.time.Duration

fun main(args: Array<String>) {
    val config = Config.fromEnvironment()
    println(config)

    val auth = JwtHmacAuthenticator("secret", Duration.ofDays(1))

    println(auth.generateJwt(User("Toefel", listOf("admin", "user"))))

    val server = GarssonApiServer(config)
    server.start()
    Runtime.getRuntime().addShutdownHook(Thread(Runnable { server.stop() }))
}