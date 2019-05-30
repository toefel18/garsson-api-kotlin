package nl.toefel.garsson.server

import nl.toefel.garsson.dto.Order
import nl.toefel.garsson.dto.OrderLine
import nl.toefel.garsson.dto.State
import nl.toefel.garsson.util.now

fun createOrder(id: String): Order {
    return Order(
        orderId = id,
        tableId = "table $id",
        clientId = "client $id",
        waiterId = "waiter $id",
        orderLines = listOf(
            OrderLine(
                quantity = 2,
                productId = id,
                productName = "Cola",
                productBrand = "Coca cola",
                productPrice = 1.80,
                quantityUnit = "BOTTLE"
            ),
            OrderLine(
                quantity = 3,
                productId = "$id-ba",
                productName = "Iced Tea",
                productBrand = "Nestle",
                productPrice = 1.90,
                quantityUnit = "BOTTLE"
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
