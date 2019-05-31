package nl.toefel.garsson.converter

import nl.toefel.garsson.dto.Product
import nl.toefel.garsson.repository.ProductEntity

object ProductConverter {

    fun toDto(product: ProductEntity): Product {
        return Product(
            id = product.id.value,
            name = product.name,
            brand = product.brand,
            barcode = product.barcode,
            unit = product.unit,
            pricePerUnit = product.pricePerUnit.setScale(2).toString(),
            purchasePricePerUnit = product.purchasePricePerUnit?.setScale(2).toString(),
            createdTime = product.createdTime,
            lastEditTime = product.lastEditTime
        )
    }
}