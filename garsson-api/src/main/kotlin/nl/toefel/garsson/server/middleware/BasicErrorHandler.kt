package nl.toefel.garsson.server.middleware

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.server.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BasicErrorHandler(val next: HttpHandler) : HttpHandler {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BasicErrorHandler::class.java)
    }

    constructor(next: HandlerFun) : this(HttpHandler { exchange -> next(exchange) })

    override fun handleRequest(exchange: HttpServerExchange?) {
        try {
            next.handleRequest(exchange)
        } catch (ex: ClientErrorException) {
            val apiError = when (ex) {
                is MissingRequiredParameter -> ApiError(status = Status.BAD_REQUEST, message = "${ex.message}")
                is InvalidParameterFormat -> ApiError(status = Status.BAD_REQUEST, message = "${ex.message}")
                is BodyParseException -> ApiError(status = Status.BAD_REQUEST, message = "${ex.message}")
                else -> throw ex
            }

            if (!exchange!!.isResponseStarted) {
                exchange.sendJsonResponse(apiError.status, apiError)
            } else {
                logger.error("Encountered a client error, but response already started! error=$apiError")
            }
        }
    }
}