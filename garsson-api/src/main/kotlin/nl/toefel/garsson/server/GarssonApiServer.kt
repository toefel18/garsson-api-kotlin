package nl.toefel.garsson.server

import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.BlockingHandler
import io.undertow.util.Headers
import io.undertow.util.HttpString
import nl.toefel.garsson.Config
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.auth.User
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.dto.LoginCredentials
import nl.toefel.garsson.dto.SuccessfulLoginResponse
import nl.toefel.garsson.dto.Version
import nl.toefel.garsson.json.Jsonizer
import nl.toefel.garsson.server.middleware.AuthHandler
import nl.toefel.garsson.server.middleware.CORSHandler
import nl.toefel.garsson.server.middleware.RequestLoggingHandler

class GarssonApiServer(val config: Config, val auth: JwtHmacAuthenticator) {

    val undertow = Undertow.builder()
            .addHttpListener(config.port, "0.0.0.0")
            .setHandler(getRoutes())
            .build()

    fun start() {
        undertow.start()
    }

    fun stop() {
        undertow.stop()
    }

    private fun getRoutes(): HttpHandler {
        return RequestLoggingHandler(
                CORSHandler(
                        Handlers.routing()
                                .get("/version", ::version)
                                .post("/api/v1/login", BlockingHandler(::login))
                                .get("/api/v1/products", AuthHandler(::listProducts, auth))
                                .get("/api/v1/orders", AuthHandler(::listOrders, auth))
                                .get("/api/v1/orders/{orderId}", AuthHandler(::getOrder, auth))
                                .setFallbackHandler(::fallback)
                                .setInvalidMethodHandler(::invalidMethod)
                )
        )
    }

    private fun login(exchange: HttpServerExchange) {
        val credentials: LoginCredentials = Jsonizer.fromJson(exchange.inputStream.readAllBytes())
        val jwt = auth.generateJwt(User(credentials.email, roles = listOf("user")))
        exchange.responseHeaders.put(HttpString("Authorization"), "Bearer ${jwt}")
        exchange.sendJson(200, SuccessfulLoginResponse(jwt))
    }

    private fun version(exchange: HttpServerExchange) {
        exchange.sendJson(200, Version.fromBuildInfo())
    }

    private fun listProducts(exchange: HttpServerExchange) {
        exchange.sendJson(200, "list products")
    }

    private fun listOrders(exchange: HttpServerExchange) {
        exchange.sendJson(200, listOf(createOrder("1"), createOrder("2")))
    }

    private fun getOrder(exchange: HttpServerExchange) {
        exchange.sendJson(200, "get order ${exchange.queryParameters["orderId"]?.first}")
    }

    private fun fallback(exchange: HttpServerExchange) {
        exchange.sendJson(Status.NOT_FOUND, ApiError("request uri not found: ${exchange.requestURI}"))
    }

    private fun invalidMethod(exchange: HttpServerExchange) {
        exchange.sendJson(Status.METHOD_NOT_ALLOWED, ApiError("method ${exchange.requestMethod} not supported on uri ${exchange.requestURI}"))
    }
}

fun HttpServerExchange.sendJson(code: Int, data: Any) {
    this.statusCode = code
    this.responseHeaders.put(Headers.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
    val body = if (data is ApiError && data.status == -1) {
        data.copy(status = this.statusCode).copy(uri = this.requestURI)
    } else {
        data
    }
    this.responseSender.send(Jsonizer.toJson(body))
}

object Status {
    val OK = 200
    val CREATED = 201
    val NO_CONTENT = 204
    val BAD_REQUEST = 400
    val UNAUTHORIZED = 401
    val FORBIDDEN = 401
    val NOT_FOUND = 404
    val METHOD_NOT_ALLOWED = 405
    val UNSUPPORED_MEDIA_TYPE = 415
    val INTERNAL_SERVER_ERROR = 500
}