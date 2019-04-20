package nl.toefel.garsson.server.middleware

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.server.Status
import nl.toefel.garsson.server.sendJson
import org.slf4j.LoggerFactory

class AuthHandler(val next: HttpHandler, val auth: JwtHmacAuthenticator) : HttpHandler {

    //    companion object : KLogging()
    companion object {
        val logger = LoggerFactory.getLogger(AuthHandler::class.java)
    }

    // allows for wrapping handlers defined as HttpServerExchange -> Unit
    constructor(next: (HttpServerExchange) -> Unit, auth: JwtHmacAuthenticator)
            : this(HttpHandler { exchange -> next(exchange) }, auth)

    override fun handleRequest(exchange: HttpServerExchange?) {
        val authHeader = exchange?.requestHeaders?.getFirst("Authorization")
        if (authHeader == null) {
            exchange!!.sendJson(Status.UNAUTHORIZED, ApiError("not authenticated, provide Authorization header with valid jwt"))
        } else {
            val token = if (authHeader.startsWith("Bearer ")) authHeader.substring(7) else authHeader
            try {
                val user = auth.extractUser(token)
                exchange.putAttachment(Keys.USER_ATTACHMENT, user)
                next.handleRequest(exchange)
            } catch (ex: Exception) {
                logger.warn("invalid Authorization header ${ex.message}")
                exchange.sendJson(Status.BAD_REQUEST, ApiError("invalid token: ${ex.message}"))
            }
        }
    }
}