package nl.toefel.garsson.server

import nl.toefel.garsson.dto.*
import nl.toefel.garsson.util.now

fun createOrder(id: String) : Order {
    return Order(
            orderId = id,
            tableId = "table $id",
            clientId = "client $id",
            waiterId = "waiter $id",
            orderLines = listOf(
                    OrderLine(
                            quantity = 2,
                            product = Product(
                                    productId = "cola1",
                                    productPrice = 1.50,
                                    quantityUnit = QuantityUnit.ITEM
                            )
                    ),
                    OrderLine(
                            quantity = 3,
                            product = Product(
                                    productId = "icedtea1",
                                    productPrice = 1.70,
                                    quantityUnit = QuantityUnit.ITEM
                            )
                    )
            ),
            totalPrice = 7.50,
            createdTime = now(),
            preparedTime = null,
            deliveredTime = null,
            paidTime = null,
            state = State.CREATED
    )
}
