package nl.toefel.garsson.server.middleware

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.PrematureJwtException
import io.jsonwebtoken.io.DecodingException
import io.jsonwebtoken.security.SignatureException
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.server.Status
import nl.toefel.garsson.server.sendJsonResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Extracts user from the Authorization header and attaches it to the request
 *
 * Does not fail if the header is not present!
 * Only fails if the authorization is malformed, has the wrong signature, is expired or is not active yet.
 */
class AuthTokenExtractorHandler(private val auth: JwtHmacAuthenticator, val next: HttpHandler) : HttpHandler {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AuthTokenExtractorHandler::class.java)
    }

    override fun handleRequest(exchange: HttpServerExchange?) {
        val authHeader = exchange?.requestHeaders?.getFirst("Authorization")
        if (authHeader != null && authHeader.isNotBlank()) {
            val token = if (authHeader.startsWith("Bearer ")) authHeader.substring(7) else authHeader
            try {
                val user = auth.extractUser(token)
                exchange.putAttachment(Attachments.USER, user)
            } catch (ex: Exception) {
                val authError = when (ex) {
                    is ExpiredJwtException -> "token expired"
                    is PrematureJwtException -> "token not yet valid"
                    is SignatureException -> "invalid signature"
                    is DecodingException -> "invalid jwt"
                    else -> "invalid token"
                }
                logger.error("$authError: ${ex.javaClass.simpleName}: ${ex.message} on URL ${exchange.requestURL}")
                exchange.sendJsonResponse(Status.BAD_REQUEST, ApiError(authError))
                return
            }
        }
        next.handleRequest(exchange)
    }
}