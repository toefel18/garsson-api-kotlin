package nl.toefel.garsson.server.middleware

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import mu.NamedKLogging


class RequestLoggingHandler(val next: HttpHandler) : HttpHandler {

    companion object : NamedKLogging("RequestLog")

    override fun handleRequest(exchange: HttpServerExchange?) {
        val start = System.currentTimeMillis()
        next.handleRequest(exchange)
        val method = exchange?.requestMethod
        val status = exchange?.statusCode
        val remoteHost = exchange?.sourceAddress?.address?.hostAddress + ":" + exchange?.sourceAddress?.port
        val query = exchange?.queryString ?: ""
        val requestUri = exchange?.requestURI + if (query.isNotEmpty()) "?$query" else ""
        val duration = System.currentTimeMillis() - start
        val user = exchange?.getAttachment(Keys.USER_ATTACHMENT)
        val userName =  " user=${user?.name ?: "anonymous"}"
        val userRoles = " userRoles=[${user?.roles?.joinToString { it } ?: ""}]"

        logger.info { "IN $method status=$status uri=$requestUri duration=$duration remoteHost=$remoteHost${userName}${userRoles}" }
    }
}