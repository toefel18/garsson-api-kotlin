package nl.toefel.garsson.server.handlers

import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.server.HandlerFun
import nl.toefel.garsson.server.createOrder
import nl.toefel.garsson.server.sendJsonResponse


fun listOrders(): HandlerFun = { exchange: HttpServerExchange ->
    exchange.sendJsonResponse(200, listOf(
        createOrder("1"),
        createOrder("2"),
        createOrder("3"),
        createOrder("4"),
        createOrder("5"),
        createOrder("6"),
        createOrder("8"),
        createOrder("9"),
        createOrder("10")))
}

fun getOrder(): HandlerFun = { exchange: HttpServerExchange ->
    exchange.sendJsonResponse(200, "get order ${exchange.queryParameters["orderId"]?.first}")
}


