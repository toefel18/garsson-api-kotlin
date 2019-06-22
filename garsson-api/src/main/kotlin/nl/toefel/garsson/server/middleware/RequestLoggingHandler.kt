package nl.toefel.garsson.server.middleware

import io.undertow.server.DefaultResponseListener.EXCEPTION
import io.undertow.server.ExchangeCompletionListener
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RequestLoggingHandler(val next: HttpHandler) : HttpHandler {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(RequestLoggingHandler::class.java)
    }

    override fun handleRequest(exchange: HttpServerExchange?) {
        val start = System.currentTimeMillis()

        /**
         * Required because requests with IO are dispatched to worker threads
         */
        exchange?.addExchangeCompleteListener { exch: HttpServerExchange?, nextListener: ExchangeCompletionListener.NextListener? ->
            val method = exch?.requestMethod
            val query = exch?.queryString ?: ""

            val status = " status=${exch?.statusCode ?: -1}"
            val remoteHost
                = " remoteHost=${exch?.sourceAddress?.address?.hostAddress + ":" + exch?.sourceAddress?.port}"
            val uri = " uri=${exch?.requestURI + if (query.isNotEmpty()) "?$query" else ""}"

            val userEnity = exch?.getAttachment(Attachments.USER)
            val user = " user=${userEnity?.name ?: "anonymous"}"
            val roles = " userRoles=[${userEnity?.roles?.joinToString { it } ?: ""}]"

            val exceptionAttachment = exch?.getAttachment(EXCEPTION)
            val exception = if (exceptionAttachment == null) "" else " exception=`${exceptionAttachment.message}`"

            // only log request and response bodies on errors, otherwise too much logging
            val requestBodyAttachment = exch?.getAttachment(Attachments.REQUEST_BODY)
            val requestBody = if (requestBodyAttachment != null && (exchange.statusCode > 299 || exchange.statusCode < 200)) " requestBody=`$requestBodyAttachment`" else ""
            val responseBodyAttachment = exch?.getAttachment(Attachments.RESPONSE_BODY)
            val responseBody = if (responseBodyAttachment != null && (exchange.statusCode > 299 || exchange.statusCode < 200)) " responseBody=`$responseBodyAttachment`" else ""

            val duration = " duration=${System.currentTimeMillis() - start}"
            logger.info("$method$status$uri$duration$user$roles$remoteHost$requestBody$responseBody$exception")

            nextListener?.proceed()
        }

        next.handleRequest(exchange)
    }
}