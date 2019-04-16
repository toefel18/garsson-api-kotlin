package nl.toefel.garsson.dto

const val UNKNOWN = ""

data class Order(
        val orderId: String,
        val tableId: String = UNKNOWN,
        val clientId: String = UNKNOWN,
        val waiterId: String = UNKNOWN,
        val orderLines: List<OrderLine>,
        val totalPrice: Double = -1.0,
        val createdTime: String?,
        val preparedTime: String? = null,
        val deliveredTime: String? = null,
        val paidTime: String? = null
)

data class OrderLine(
        val quantity: Int = 1,
        val product: Product
)

data class Product(
        val productId: String,
        val productPrice: Double,
        val quantityUnit: QuantityUnit
)

enum class QuantityUnit {
    ITEM,
    LITER,
    GRAM
}

enum class State{
    CREATED,
    PREPARING,
    READY,
    DELIVERED,
    PAID,
}
