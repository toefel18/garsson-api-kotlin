package nl.toefel.garsson.server

import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.UndertowOptions
import io.undertow.server.HttpHandler
import nl.toefel.garsson.Config
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.server.handlers.*
import nl.toefel.garsson.server.middleware.AuthTokenExtractorHandler
import nl.toefel.garsson.server.middleware.CORSHandler
import nl.toefel.garsson.server.middleware.UnexpectedErrorHandler
import nl.toefel.garsson.server.middleware.RequestLoggingHandler
import org.slf4j.LoggerFactory

/**
 * An API router based on Undertow.
 * Listens on all IP addresses of the machine (0.0.0.0)
 * The getRoutes method contains all the supported URL's by the server and dispatches
 * them to handlers. There are several handlers that wrap the others to implement common
 * functionality, such as logging, CORS, authentication and exception handling.
 *
 * Routes can:
 * 1. support path variables which can be read by the handlers
 * 2. be guarded with roles by using the extension function [requiresRole]
 * 3. must be postfixed with .[blocks] if it requires IO (like reading the request or going to the database.
 */
class GarssonRouter(private val config: Config, val auth: JwtHmacAuthenticator) {
    val logger = LoggerFactory.getLogger(GarssonRouter::class.java)

    val undertow: Undertow = Undertow.builder()
        .addHttpListener(config.port, "0.0.0.0")
        .setServerOption(UndertowOptions.ENABLE_STATISTICS, true)
        .setHandler(getRoutes())
        .build()

    fun start() {
        logger.info("Starting api router on port ${config.port}")
        undertow.start()
        logger.info("Api router started!")
    }

    fun stop() {
        logger.info("Stopping api router")
        undertow.stop()
        logger.info("api router stopped")
    }

    private fun getRoutes(): HttpHandler {
        /**
         * Keep in mind when chaining handlers that when leaf handlers do IO (.blocks) the exchange is dispatched
         * a XNIO worker thread. This means that another thread continues the exchange and the the handler chain
         * on the IO thread is returned. See [RequestLoggingHandler] or [UnexpectedErrorHandler] for how they handle
         * these cases.
         */
        return RequestLoggingHandler(
            UnexpectedErrorHandler(
                CORSHandler(
                    AuthTokenExtractorHandler(auth,
                        Handlers.routing()
                            .get("/version", version())
                            .get("/statistics", statistics(this))

                            .post("/api/v1/login", login(auth).basicErrors.blocks)

                            .get("/api/v1/products", listProducts().basicErrors.blocks)
                            .post("/api/v1/products", addProduct().basicErrors.blocks)
                            .get("/api/v1/products/{productId}", getProduct().basicErrors.blocks)
                            .put("/api/v1/products/{productId}", updateProduct().basicErrors.blocks)
                            .delete("/api/v1/products/{productId}", deleteProduct().basicErrors.blocks)

                            .get("/api/v1/orders", listOrders().basicErrors requiresRole "user")
                            .get("/api/v1/orders/{orderId}", getOrder().basicErrors requiresRole "user")
                            .get("/api/v1/orders-updates", orderUpdates().basicErrors requiresRole "user")

                            .setFallbackHandler(fallback())
                            .setInvalidMethodHandler(invalidMethod())
                    )
                )
            )
        )
    }
}

