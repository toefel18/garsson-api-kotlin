package nl.toefel.garsson.server

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.BlockingHandler
import io.undertow.util.Headers
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.json.Jsonizer
import nl.toefel.garsson.server.middleware.RequireRoleHandler

fun HttpServerExchange.sendJson(code: Int, data: Any) {
    this.statusCode = code
    this.responseHeaders.put(Headers.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
    val body = if (data is ApiError && data.status == -1) {
        data.copy(status = this.statusCode).copy(uri = this.requestURI)
    } else {
        data
    }
    this.responseSender.send(Jsonizer.toJson(body))
}

/** Alias for a handler that is a function reference */
typealias HandlerFun = (HttpServerExchange) -> Unit

/** Adds a role requirement to a HandlerFun */
infix fun HandlerFun.requiresRole(role: String) = RequireRoleHandler(listOf(role), this)

/** Adds a role requirement to a HttpHandler */
infix fun HttpHandler.requiresRole(role: String) = RequireRoleHandler(listOf(role), this)

/** Wraps the handler in a BlockingHandler (a blocking handler dispatches the request to a worker thread) */
val HttpHandler.blocks get() = BlockingHandler(this)

/** Wraps the handler in a BlockingHandler (a blocking handler dispatches the request to a worker thread) */
val HandlerFun.blocks get() = BlockingHandler(this)

