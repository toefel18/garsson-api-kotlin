package nl.toefel.garsson.server.handlers

import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.converter.ProductConverter
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.dto.Product
import nl.toefel.garsson.repository.ProductEntity
import nl.toefel.garsson.repository.ProductsTable
import nl.toefel.garsson.server.BodyParseException
import nl.toefel.garsson.server.HandlerFun
import nl.toefel.garsson.server.Status
import nl.toefel.garsson.server.readRequestBody
import nl.toefel.garsson.server.sendJsonResponse
import nl.toefel.garsson.util.now
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal


fun listProducts(): HandlerFun = { exchange: HttpServerExchange ->
    val allProductsDtos = transaction {
        val allProducts = ProductEntity.all()
        allProducts.map { ProductConverter.toDto(it) }
    }

    exchange.sendJsonResponse(200, allProductsDtos)
}

fun addProduct(): HandlerFun = { exchange: HttpServerExchange ->
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
fun getProduct(): HandlerFun = { exchange: HttpServerExchange ->
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

fun updateProduct(): HandlerFun = { exchange: HttpServerExchange ->
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
                    productEntity.barcode = updatedProduct.barcode ?: productEntity.barcode
                    productEntity.unit = updatedProduct.unit
                    productEntity.pricePerUnit = BigDecimal(updatedProduct.pricePerUnit)
                    productEntity.purchasePricePerUnit = updatedProduct.purchasePricePerUnit?.let { BigDecimal(it) }
                        ?: productEntity.purchasePricePerUnit
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

fun deleteProduct(): HandlerFun = { exchange: HttpServerExchange ->
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