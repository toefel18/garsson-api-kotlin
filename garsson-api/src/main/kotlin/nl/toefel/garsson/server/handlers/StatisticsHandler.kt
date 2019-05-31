package nl.toefel.garsson.server.handlers

import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.server.GarssonRouter
import nl.toefel.garsson.server.HandlerFun
import nl.toefel.garsson.server.sendJsonResponse

fun statistics(router: GarssonRouter): HandlerFun = { exchange: HttpServerExchange ->
    val stats = router.undertow.listenerInfo.first().connectorStatistics
    exchange.sendJsonResponse(200, stats)
}
