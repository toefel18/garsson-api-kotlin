package nl.toefel.garsson

fun main(args: Array<String>) {
    val server = Server(8080)
    server.start()

    Runtime.getRuntime().addShutdownHook(Thread(Runnable { server.stop() }))
}