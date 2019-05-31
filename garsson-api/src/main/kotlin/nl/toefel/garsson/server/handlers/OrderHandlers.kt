package nl.toefel.garsson.server.handlers

import io.undertow.Handlers
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.websockets.core.AbstractReceiveListener
import io.undertow.websockets.core.BufferedTextMessage
import io.undertow.websockets.core.WebSocketCallback
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import nl.toefel.garsson.server.HandlerFun
import nl.toefel.garsson.server.createOrder
import nl.toefel.garsson.server.sendJsonResponse


fun listOrders(): HandlerFun = { exchange: HttpServerExchange ->
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

fun getOrder(): HandlerFun = { exchange: HttpServerExchange ->
    exchange.sendJsonResponse(200, "get order ${exchange.queryParameters["orderId"]?.first}")
}


val orderUpdateSockets = mutableListOf<WebSocketChannel>()

fun orderUpdates(): HttpHandler {
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


