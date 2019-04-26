package nl.toefel.garsson.server.middleware

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.server.HandlerFun
import nl.toefel.garsson.server.Status
import nl.toefel.garsson.server.sendJson
import org.slf4j.LoggerFactory

class RequireRoleHandler(val requiredRoles: List<String>, val next: HttpHandler) : HttpHandler {

    companion object {
        val logger = LoggerFactory.getLogger(RequireRoleHandler::class.java)
    }

    // allows for wrapping handlers defined as HttpServerExchange -> Unit
    constructor(requiredRoles: List<String>, next: HandlerFun)
        : this(requiredRoles, HttpHandler { exchange -> next(exchange) })

    override fun handleRequest(exchange: HttpServerExchange?) {
        val user = exchange?.getAttachment(Attachments.USER)
        if (user == null) {
            exchange!!.sendJson(Status.UNAUTHORIZED, ApiError("not authenticated, provide Authorization header with valid jwt"))
        } else if (!user.roles.containsAll(requiredRoles)) {
            val missingRoles = requiredRoles.filter { requiredRole -> !user.roles.contains(requiredRole) }
            exchange.sendJson(Status.FORBIDDEN, ApiError("missing required roles '${missingRoles.joinToString()}', available roles '${user.roles.joinToString()}'"))
        } else {
            next.handleRequest(exchange)
        }
    }
}