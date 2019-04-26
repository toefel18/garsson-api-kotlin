package nl.toefel.garsson.server.middleware

import io.undertow.server.DefaultResponseListener
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.server.Status
import nl.toefel.garsson.server.sendJson
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
        return if (!exchange.isResponseStarted) {
            logger.error("Uncaught exception, responding with 500, uri=${exchange.requestURI}", ex)
            val errorMessage = if (ex != null) "Uncaught exception ${ex::class.qualifiedName}: ${ex.message}" else "Unknown message occurred"
            exchange.sendJson(Status.INTERNAL_SERVER_ERROR, ApiError(errorMessage))
            true // this handler generated a response
        } else {
            logger.error("Uncaught exception but response has already been started", ex)
            false // this handler did not generate a response
        }
    }
}