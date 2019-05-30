package nl.toefel.garsson.server

import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.HttpString
import io.undertow.websockets.core.*
import nl.toefel.garsson.Config
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.dto.*
import nl.toefel.garsson.repository.ProductEntity
import nl.toefel.garsson.repository.ProductsTable
import nl.toefel.garsson.repository.UserEntity
import nl.toefel.garsson.repository.UsersTable
import nl.toefel.garsson.server.middleware.AuthTokenExtractor
import nl.toefel.garsson.server.middleware.CORSHandler
import nl.toefel.garsson.server.middleware.ExceptionErrorHandler
import nl.toefel.garsson.server.middleware.RequestLoggingHandler
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

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
                            .get("/api/v1/orders-updates", orderUpdates() requiresRole "user")
                            .setFallbackHandler(::fallback)
                            .setInvalidMethodHandler(::invalidMethod)
                    )
                )
            )
        )
    }

    val orderUpdateSockets = mutableListOf<WebSocketChannel>()

    private fun orderUpdates(): HttpHandler {
        return Handlers.websocket { exchange, channel ->
            orderUpdateSockets.add(channel)

            channel.addCloseTask { channel -> orderUpdateSockets.remove(channel) }

            channel.receiveSetter.set { listener ->
                object : AbstractReceiveListener() {
                    override fun onFullTextMessage(channel: WebSocketChannel?, message: BufferedTextMessage?) {
                        WebSockets.sendText("Hellow Undertow", channel, null)
                    }
                }
            }

            channel.resumeReceives()
            WebSockets.sendText("Hellow Undertow2", channel, null)
            Thread {
                println("SLEEPING")
                Thread.sleep(5000)
                println("SENDING")
                WebSockets.sendText("Hellow Undertowz", channel, object : WebSocketCallback<Void> {
                    override fun complete(channel: WebSocketChannel?, context: Void?) {
                        println("second send complete")
                    }

                    override fun onError(channel: WebSocketChannel?, context: Void?, throwable: Throwable?) {
                        println("second send error")
                        throwable?.printStackTrace()
                    }
                })
            }.start()
        }
    }


    private fun version(exchange: HttpServerExchange) {
        exchange.sendJsonResponse(200, Version.fromBuildInfo())
    }

    private fun login(exchange: HttpServerExchange) {
        try {
            val credentials: LoginCredentials = exchange.readRequestBody()

            transaction {
                val user = UserEntity.find { UsersTable.email eq credentials.email}.firstOrNull()
                when {
                    user == null -> exchange.sendJsonResponse(Status.UNAUTHORIZED, ApiError("user not found"))
                    user.password == credentials.password -> {
                        user.lastLoginTime = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).toString()
                        val jwt = auth.generateJwt(User(credentials.email, roles = user.roles.split(",").toList()))
                        exchange.responseHeaders.put(HttpString("Authorization"), "Bearer $jwt")
                        exchange.sendJsonResponse(Status.OK, SuccessfulLoginResponse(jwt))
                    }
                    else -> exchange.sendJsonResponse(Status.UNAUTHORIZED, ApiError("invalid password"))
                }
            }
        } catch (ex: BodyParseException) {
            exchange.sendJsonResponse(Status.BAD_REQUEST, ApiError(ex.message!!))
        }
    }

    private fun listProducts(exchange: HttpServerExchange) {
        val allProductsDtos = transaction {
            val allProducts = ProductEntity.all()
            allProducts.map { product -> Product(
                id = product.id.value,
                name = product.name,
                brand = product.brand,
                barcode = product.barcode,
                unit = product.unit,
                pricePerUnit = product.pricePerUnit.setScale(2).toString(),
                purchasePricePerUnit = product.purchasePricePerUnit?.setScale(2).toString(),
                createdTime = product.createdTime,
                lastEditTime = product.lastEditTime
            ) }
        }

        exchange.sendJsonResponse(200, allProductsDtos)
    }

    private fun listOrders(exchange: HttpServerExchange) {
        exchange.sendJsonResponse(200, listOf(
            createOrder("1"),
            createOrder("2"),
            createOrder("3"),
            createOrder("4"),
            createOrder("5"),
            createOrder("6"),
            createOrder("8"),
            createOrder("9"),
            createOrder("10")))
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

