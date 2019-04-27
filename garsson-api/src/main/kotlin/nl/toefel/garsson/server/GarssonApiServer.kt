package nl.toefel.garsson.server

import com.fasterxml.jackson.core.JsonParseException
import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.RequestDumpingHandler
import io.undertow.server.handlers.StoredResponseHandler
import io.undertow.server.handlers.form.EagerFormParsingHandler
import io.undertow.util.HttpString
import nl.toefel.garsson.Config
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.auth.User
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.dto.LoginCredentials
import nl.toefel.garsson.dto.SuccessfulLoginResponse
import nl.toefel.garsson.dto.Version
import nl.toefel.garsson.server.middleware.AuthTokenExtractor
import nl.toefel.garsson.server.middleware.CORSHandler
import nl.toefel.garsson.server.middleware.ExceptionErrorHandler
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
        /**
         * Keep in mind when chaining handlers that when leaf handlers do IO (.blocks) the exchange is dispatched
         * a XNIO worker thread. This means that another thread continues the exchange and the the handler chain
         * on the IO thread is returned. See [RequestLoggingHandler] or [ExceptionErrorHandler] for how they handle
         * these cases.
         */
        return RequestLoggingHandler(
            ExceptionErrorHandler(
                CORSHandler(
                    AuthTokenExtractor(auth,
                        Handlers.routing()
                            .get("/version", ::version)
                            .post("/api/v1/login", ::login.blocks)
                            .get("/api/v1/products", ::listProducts.blocks)
                            .get("/api/v1/orders", ::listOrders requiresRole "user")
                            .get("/api/v1/orders/{orderId}", ::getOrder requiresRole "user")
                            .setFallbackHandler(::fallback)
                            .setInvalidMethodHandler(::invalidMethod)
                    )
                )
            )
        )
    }

    private fun login(exchange: HttpServerExchange) {
        try {
            val credentials: LoginCredentials = exchange.readRequestBody()
            val jwt = auth.generateJwt(User(credentials.email, roles = listOf("user")))
            exchange.responseHeaders.put(HttpString("Authorization"), "Bearer ${jwt}")
            exchange.sendJsonResponse(200, SuccessfulLoginResponse(jwt))
        } catch (ex : BodyParseException) {
            exchange.sendJsonResponse(Status.BAD_REQUEST, ApiError(ex.message!!))
        }
    }

    private fun version(exchange: HttpServerExchange) {
        exchange.sendJsonResponse(200, Version.fromBuildInfo())
    }

    private fun listProducts(exchange: HttpServerExchange) {
        exchange.sendJsonResponse(200, "list products")
    }

    private fun listOrders(exchange: HttpServerExchange) {
        exchange.sendJsonResponse(200, listOf(createOrder("1"), createOrder("2")))
    }

    private fun getOrder(exchange: HttpServerExchange) {
        exchange.sendJsonResponse(200, "get order ${exchange.queryParameters["orderId"]?.first}")
    }

    private fun fallback(exchange: HttpServerExchange) {
        exchange.sendJsonResponse(Status.NOT_FOUND, ApiError("request uri not found: ${exchange.requestURI}"))
    }

    private fun invalidMethod(exchange: HttpServerExchange) {
        exchange.sendJsonResponse(Status.METHOD_NOT_ALLOWED, ApiError("method ${exchange.requestMethod} not supported on uri ${exchange.requestURI}"))
    }
}

