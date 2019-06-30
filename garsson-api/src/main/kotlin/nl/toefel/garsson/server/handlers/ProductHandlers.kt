package nl.toefel.garsson.server.handlers

import io.undertow.server.HttpServerExchange
import nl.toefel.garsson.converter.ProductConverter
import nl.toefel.garsson.dto.ApiError
import nl.toefel.garsson.dto.Product
import nl.toefel.garsson.repository.ProductEntity
import nl.toefel.garsson.repository.ProductsTable
import nl.toefel.garsson.server.*
import nl.toefel.garsson.util.now
import org.jetbrains.exposed.sql.transactions.transaction


fun listProducts(): HandlerFun = { exchange: HttpServerExchange ->
    val allProductsDtos = transaction {
        val allProducts = ProductEntity.all()
        allProducts.map { ProductConverter.toDto(it) }
    }

    exchange.sendJsonResponse(200, allProductsDtos)
}

fun getProduct(): HandlerFun = { exchange: HttpServerExchange ->
    val productId = exchange.requireParamAsLong("productId")

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

fun addProduct(): HandlerFun = { exchange: HttpServerExchange ->
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
                pricePerUnit = toBigDecimal("pricePerUnit", newProduct.pricePerUnit)
                purchasePricePerUnit = newProduct.purchasePricePerUnit?.let {
                    toBigDecimal("purchasePricePerUnit", it)
                }
                createdTime = now()
                lastEditTime = now()
            }
            commit()
            val productDto = ProductConverter.toDto(createdProductEntity)
            exchange.sendJsonResponse(Status.OK, productDto)
        }
    }
}

fun updateProduct(): HandlerFun = { exchange: HttpServerExchange ->
    val productId = exchange.requireParamAsLong("productId")
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
            productEntity.pricePerUnit = toBigDecimal("pricePerUnit", updatedProduct.pricePerUnit)
            productEntity.purchasePricePerUnit = updatedProduct.purchasePricePerUnit?.let {
                toBigDecimal("purchasePricePerUnit", it)
            } ?: productEntity.purchasePricePerUnit
            productEntity.lastEditTime = now()
            commit()
            val productDto = ProductConverter.toDto(productEntity)
            exchange.sendJsonResponse(Status.OK, productDto)
        }
    }
}

fun deleteProduct(): HandlerFun = { exchange: HttpServerExchange ->
    val productId = exchange.requireParamAsLong("productId")
    transaction {
        val productEntity = ProductEntity.findById(productId)
        if (productEntity == null) {
            exchange.sendJsonResponse(Status.NOT_FOUND, ApiError("product with id $productId does not exist"))
        } else {
            val productDto = ProductConverter.toDto(productEntity)
            productEntity.delete()
            commit()
            exchange.sendJsonResponse(Status.OK, productDto)
        }
    }
}