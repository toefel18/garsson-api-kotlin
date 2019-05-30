package nl.toefel.garsson.dto

data class Product(
    val id: Long,
    val name: String,
    val brand: String,
    val barcode: String?,
    val unit: String,
    val pricePerUnit: String,
    val purchasePricePerUnit: String?,
    val createdTime: String,
    val lastEditTime: String?
)