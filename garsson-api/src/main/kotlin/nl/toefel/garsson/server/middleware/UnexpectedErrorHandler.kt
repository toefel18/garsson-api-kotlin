package nl.toefel.garsson.server.middleware

import io.undertow.server.DefaultResponseListener
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.server.Status
import nl.toefel.garsson.server.sendJsonResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Handles exceptions for sync and async calls. Exceptions are reported to the user via an
 * [ApiError] JSON with a 500 status code.
 */
class UnexpectedErrorHandler(val next: HttpHandler) : HttpHandler {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(UnexpectedErrorHandler::class.java)
    }

    override fun handleRequest(exchange: HttpServerExchange?) {
        try {
            exchange?.addDefaultResponseListener { exchangeInHandler ->
                val ex = exchangeInHandler.getAttachment(DefaultResponseListener.EXCEPTION)
                if (ex != null) handleError(exchangeInHandler, ex) else false
            }
            next.handleRequest(exchange)
        } catch (ex: Throwable) {
            handleError(exchange!!, ex)
        }
    }

    private fun handleError(exchange: HttpServerExchange, ex: Throwable?): Boolean {
        val apiError = when (ex) {
            // only for unexpected errors, other errors should be handled by the handlers themselves or BasicErrorHandler
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