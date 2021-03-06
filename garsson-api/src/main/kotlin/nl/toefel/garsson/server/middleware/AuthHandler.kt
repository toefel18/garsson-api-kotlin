package nl.toefel.garsson.server.middleware

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.server.HandlerFun
import nl.toefel.garsson.server.Status
import nl.toefel.garsson.server.sendJsonResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AuthHandler(val next: HttpHandler, val auth: JwtHmacAuthenticator) : HttpHandler {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AuthHandler::class.java)
    }

    // allows for wrapping handlers defined as HttpServerExchange -> Unit
    constructor(next: HandlerFun, auth: JwtHmacAuthenticator)
        : this(HttpHandler { exchange -> next(exchange) }, auth)

    override fun handleRequest(exchange: HttpServerExchange?) {
        val authHeader = exchange?.requestHeaders?.getFirst("Authorization")
        if (authHeader == null) {
            exchange!!.sendJsonResponse(Status.UNAUTHORIZED, ApiError("not authenticated, provide Authorization header with valid jwt"))
        } else {
            val token = if (authHeader.startsWith("Bearer ")) authHeader.substring(7) else authHeader
            try {
                val user = auth.extractUser(token)
                exchange.putAttachment(Attachments.USER, user)
                next.handleRequest(exchange)
            } catch (ex: Exception) {
                logger.warn("invalid Authorization header ${ex.message} ${ex.javaClass.simpleName}", ex)
                exchange.sendJsonResponse(Status.BAD_REQUEST, ApiError("invalid token: ${ex.message}"))
            }
        }
    }
}