package nl.toefel.garsson

import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
import java.nio.charset.Charset

class Server(val port: Int) {

    val undertow = Undertow.builder()
            .addHttpListener(port, "0.0.0.0")
            .setHandler(getRoutes())
            .build()

    fun start() {
        undertow.start()
    }

    fun stop() {
        undertow.stop()
    }

    private fun getRoutes(): RoutingHandler? {
        return Handlers.routing()
                .post("/api/login", ::login)
                .get("/api/products", ::listProducts)
                .get("/api/orders", ::listOrders)
                .get("/api/orders/{orderId}", ::getOrder)
    }

    private fun login(exchange: HttpServerExchange) {
        exchange.responseSender.send("login", Charset.defaultCharset())
    }

    private fun listProducts(exchange: HttpServerExchange) {
        exchange.responseSender.send("list products", Charset.defaultCharset())
    }

    private fun listOrders(exchange: HttpServerExchange) {
        exchange.responseSender.send("list orders", Charset.defaultCharset())
    }

    private fun getOrder(exchange: HttpServerExchange) {
        exchange.responseSender.send("get order ${exchange.queryParameters["orderId"]?.first}", Charset.defaultCharset())
    }
}