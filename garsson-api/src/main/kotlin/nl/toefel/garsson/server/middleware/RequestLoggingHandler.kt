package nl.toefel.garsson.server.middleware

import io.undertow.server.DefaultResponseListener.EXCEPTION
import io.undertow.server.ExchangeCompletionListener
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import org.slf4j.LoggerFactory

class RequestLoggingHandler(val next: HttpHandler) : HttpHandler {
    companion object {
        val logger = LoggerFactory.getLogger(RequestLoggingHandler::class.java)
    }

    override fun handleRequest(exchange: HttpServerExchange?) {
        val start = System.currentTimeMillis()

        /**
         * Required because requests with IO can be dispatched to worker threads
         */
        exchange?.addExchangeCompleteListener {
            completedExchange: HttpServerExchange?, nextListener: ExchangeCompletionListener.NextListener? ->
            val method = completedExchange?.requestMethod
            val status = completedExchange?.statusCode
            val remoteHost = completedExchange?.sourceAddress?.address?.hostAddress + ":" + completedExchange?.sourceAddress?.port
            val query = completedExchange?.queryString ?: ""
            val requestUri = completedExchange?.requestURI + if (query.isNotEmpty()) "?$query" else ""
            val duration = System.currentTimeMillis() - start
            val user = completedExchange?.getAttachment(Attachments.USER)
            val userName = " user=${user?.name ?: "anonymous"}"
            val userRoles = " userRoles=[${user?.roles?.joinToString { it } ?: ""}]"
            val exception = completedExchange?.getAttachment(EXCEPTION)
            val exceptionMsg = if (exception == null) "" else " exception=${exception.message}"

            logger.info("IN $method status=$status uri=$requestUri duration=$duration remoteHost=$remoteHost${userName}${userRoles}${exceptionMsg}")

            nextListener?.proceed()
        }

        next.handleRequest(exchange)
    }
}