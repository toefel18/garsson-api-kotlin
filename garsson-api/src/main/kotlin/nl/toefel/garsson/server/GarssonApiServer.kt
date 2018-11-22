package nl.toefel.garsson.server

import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
import io.undertow.util.Headers
import nl.toefel.garsson.Config
import nl.toefel.garsson.json.Jsonizer
import nl.toefel.garsson.server.middleware.RequestLoggingHandler

class GarssonApiServer(config: Config) {

    val undertow = Undertow.builder()
            .addHttpListener(config.port, "0.0.0.0")
            .setHandler(RequestLoggingHandler(getRoutes()))
            .build()

    fun start() {
        undertow.start()
    }

    fun stop() {
        undertow.stop()
    }

    private fun getRoutes(): RoutingHandler {
        return Handlers.routing()
                .post("/api/login", ::login)
                .get("/api/products", ::listProducts)
                .get("/api/orders", ::listOrders)
                .get("/api/orders/{orderId}", ::getOrder)
    }

    private fun login(exchange: HttpServerExchange) {
        exchange.sendJson(200, "Hello")
    }

    private fun listProducts(exchange: HttpServerExchange) {
        exchange.sendJson(200, "list products")
    }

    private fun listOrders(exchange: HttpServerExchange) {
        exchange.sendJson(200, "list orders")
    }

    private fun getOrder(exchange: HttpServerExchange) {
        exchange.sendJson(200, "get order ${exchange.queryParameters["orderId"]?.first}")
    }
}

fun HttpServerExchange.sendJson(code:Int, body:Any) {
    this.statusCode = code
    this.responseHeaders.put(Headers.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
    this.responseSender.send(Jsonizer.toJson(body))
}