package nl.toefel.garsson

fun main(args: Array<String>) {
    val config = Config.fromEnvironment()
    println(config)

    val server = GarssonApiServer(config)
    server.start()
    Runtime.getRuntime().addShutdownHook(Thread(Runnable { server.stop() }))
}