package nl.toefel.garsson.server.middleware

import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.HttpString

class CORSHandler(val next: HttpHandler) : HttpHandler {

    override fun handleRequest(exchange: HttpServerExchange?) {
        exchange?.responseHeaders?.put(HttpString("Access-Control-Allow-Origin"), "*")
        exchange?.responseHeaders?.put(HttpString("Access-Control-Max-Age"), "86400")
        exchange?.responseHeaders?.put(HttpString("Access-Control-Allow-Methods"), "HEAD, GET, POST, PUT, PATCH, DELETE, OPTIONS")
        exchange?.responseHeaders?.put(HttpString("Access-Control-Allow-Headers"), "Origin, Content-Type, Content-Length, Authorization")
        exchange?.responseHeaders?.put(HttpString("Access-Control-Expose-Headers"), "Content-Length, Authorization")
        exchange?.responseHeaders?.put(HttpString("Access-Control-Allow-Credentials"), "true")
        if ("OPTIONS" != exchange?.requestMethod?.toString()) {
            next.handleRequest(exchange)
        }
    }
}