package nl.toefel.garsson.server

import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.HttpString
import io.undertow.websockets.core.*
import nl.toefel.garsson.Config
import nl.toefel.garsson.auth.JwtHmacAuthenticator
import nl.toefel.garsson.converter.ProductConverter
import nl.toefel.garsson.dto.*
import nl.toefel.garsson.repository.*
import nl.toefel.garsson.server.middleware.AuthTokenExtractor
import nl.toefel.garsson.server.middleware.CORSHandler
import nl.toefel.garsson.server.middleware.ExceptionErrorHandler
import nl.toefel.garsson.server.middleware.RequestLoggingHandler
import nl.toefel.garsson.util.now
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.lang.NumberFormatException
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class GarssonApiServer(val config: Config, val auth: JwtHmacAuthenticator) {
    val logger = LoggerFactory.getLogger(GarssonApiServer::class.java)

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
                            .post("/api/v1/products", ::addProduct.blocks)
                            .get("/api/v1/products/{productId}", ::getProduct.blocks)
                            .put("/api/v1/products/{productId}", ::updateProduct.blocks)
                            .delete("/api/v1/products/{productId}", ::deleteProduct.blocks)

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
            allProducts.map { ProductConverter.toDto(it) }
        }

        exchange.sendJsonResponse(200, allProductsDtos)
    }

    private fun addProduct(exchange: HttpServerExchange) {
        try {
            val newProduct: Product = exchange.readRequestBody()

            transaction {
                val existingProductWithBarcode = ProductEntity.find { ProductsTable.barcode eq newProduct.barcode }.firstOrNull()

                if (existingProductWithBarcode != null) {
                    exchange.sendJsonResponse(Status.BAD_REQUEST, ApiError("barcode already exists on product with id ${existingProductWithBarcode.id}"))
                } else {
                    val createdProductEntity = ProductEntity.new {
                        name = newProduct.name
                        brand = newProduct.brand
                        barcode = newProduct.barcode
                        unit = newProduct.unit
                        pricePerUnit = BigDecimal(newProduct.pricePerUnit)
                        purchasePricePerUnit = newProduct.purchasePricePerUnit?.let { BigDecimal(it) }
                        createdTime = now()
                        lastEditTime = now()
                    }
                    val productDto = ProductConverter.toDto(createdProductEntity)
                    exchange.sendJsonResponse(Status.OK, productDto)
                }
            }
        } catch (ex: BodyParseException) {
            exchange.sendJsonResponse(Status.BAD_REQUEST, ApiError(ex.message!!))
        } catch (ex: NumberFormatException) {
            exchange.sendJsonResponse(Status.BAD_REQUEST, ApiError(ex.message!!))
        }
    }

    // re-use code with delete
    private fun getProduct(exchange: HttpServerExchange) {
        val productIdString = exchange.queryParameters["productId"]?.first
        val productId = productIdString?.toLongOrNull()
        if (productId == null) {
            exchange.sendJsonResponse(Status.BAD_REQUEST, ApiError("product id must be a number but was: $productIdString"))
        } else {
            transaction {
                val productEntity = ProductEntity.findById(productId)
                if (productEntity == null) {
                    exchange.sendJsonResponse(Status.NOT_FOUND, ApiError("product with id $productId does not exist"))
                } else {
                    val productDto = ProductConverter.toDto(productEntity)
                    exchange.sendJsonResponse(Status.OK, productDto)
                }
            }
        }
    }

    private fun updateProduct(exchange: HttpServerExchange) {
        val productIdString = exchange.queryParameters["productId"]?.first
        val productId = productIdString?.toLongOrNull()
        if (productId == null) {
            exchange.sendJsonResponse(Status.BAD_REQUEST, ApiError("product id must be a number but was: $productIdString"))
        } else {
            try {
                val updatedProduct: Product = exchange.readRequestBody()
                transaction {
                    val productEntity = ProductEntity.findById(productId)
                    if (productEntity == null) {
                        exchange.sendJsonResponse(Status.NOT_FOUND, ApiError("product with id $productId does not exist"))
                    } else {
                        productEntity.name = updatedProduct.name
                        productEntity.brand = updatedProduct.brand
                        productEntity.barcode =  updatedProduct.barcode ?: productEntity.barcode
                        productEntity.unit = updatedProduct.unit
                        productEntity.pricePerUnit = BigDecimal(updatedProduct.pricePerUnit)
                        productEntity.purchasePricePerUnit = updatedProduct.purchasePricePerUnit?.let { BigDecimal(it) } ?: productEntity.purchasePricePerUnit
                        productEntity.lastEditTime = now()
                        val productDto = ProductConverter.toDto(productEntity)
                        exchange.sendJsonResponse(Status.OK, productDto)
                    }
                }

            } catch (ex: BodyParseException) {
                exchange.sendJsonResponse(Status.BAD_REQUEST, ApiError(ex.message!!))
            } catch (ex: NumberFormatException) {
                exchange.sendJsonResponse(Status.BAD_REQUEST, ApiError(ex.message!!))
            }
        }
    }

    private fun deleteProduct(exchange: HttpServerExchange) {
        val productIdString = exchange.queryParameters["productId"]?.first
        val productId = productIdString?.toLongOrNull()
        if (productId == null) {
            exchange.sendJsonResponse(Status.BAD_REQUEST, ApiError("product id must be a number but was: $productIdString"))
        } else {
            transaction {
                val productEntity = ProductEntity.findById(productId)
                if (productEntity == null) {
                    exchange.sendJsonResponse(Status.NOT_FOUND, ApiError("product with id $productId does not exist"))
                } else {
                    val productDto = ProductConverter.toDto(productEntity)
                    productEntity.delete()
                    exchange.sendJsonResponse(Status.OK, productDto)
                }
            }
        }
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

