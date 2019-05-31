package nl.toefel.garsson.server.handlers

import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.server.HandlerFun
import nl.toefel.garsson.server.Status
import nl.toefel.garsson.server.sendJsonResponse

fun fallback(): HandlerFun = { exchange: HttpServerExchange ->
    exchange.sendJsonResponse(Status.NOT_FOUND, ApiError("request uri not found: ${exchange.requestURI}"))
}

fun invalidMethod(): HandlerFun = { exchange: HttpServerExchange ->
    exchange.sendJsonResponse(Status.METHOD_NOT_ALLOWED, ApiError("method ${exchange.requestMethod} not supported on uri ${exchange.requestURI}"))
}