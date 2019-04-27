package nl.toefel.garsson.server.middleware

import io.undertow.server.DefaultResponseListener
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.server.BodyParseException
import nl.toefel.garsson.server.Status
import nl.toefel.garsson.server.sendJsonResponse
import org.slf4j.LoggerFactory

/**
 * Handles exceptions for sync and async calls. Exceptions are reported to the user via an
 * [ApiError] JSON with a 500 status code.
 */
class ExceptionErrorHandler(val next: HttpHandler) : HttpHandler {
    companion object {
        val logger = LoggerFactory.getLogger(ExceptionErrorHandler::class.java)
    }

    override fun handleRequest(exchange: HttpServerExchange?) {
        try {
            exchange?.addDefaultResponseListener { exchangeInHandler ->
                val ex = exchangeInHandler.getAttachment(DefaultResponseListener.EXCEPTION)
                handleError(exchangeInHandler, ex)
            }
            next.handleRequest(exchange)
        } catch (ex: Throwable) {
            handleError(exchange!!, ex)
        }
    }

    private fun handleError(exchange: HttpServerExchange, ex: Throwable?): Boolean {
        // create appropriate response for common errors
        val apiError = when (ex) {
            // fallback type, should be handled by code!
            is BodyParseException -> ApiError(status = Status.BAD_REQUEST, message = "${ex.message}")
            null -> ApiError(status = Status.INTERNAL_SERVER_ERROR, message = "unknown error")
            else -> ApiError(status = Status.INTERNAL_SERVER_ERROR, message = "uncaught exception ${ex::class.qualifiedName}: ${ex.message}")
        }

        logger.error("Unexpected error: ${apiError.message}", ex)

        return if (!exchange.isResponseStarted) {
            exchange.sendJsonResponse(apiError.status, apiError)
            true // this handler generated a response
        } else {
            logger.error("Unexpected error, but response already started with status=${exchange.statusCode}")
            false // this handler did not generate a response
        }
    }
}