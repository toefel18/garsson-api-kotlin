package nl.toefel.garsson.server.handlers

import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.dto.Version
import nl.toefel.garsson.server.HandlerFun
import nl.toefel.garsson.server.sendJsonResponse

fun version(): HandlerFun = { exchange: HttpServerExchange ->
    exchange.sendJsonResponse(200, Version.fromBuildInfo())
}
