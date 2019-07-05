package nl.toefel.garsson.server.handlers

import io.undertow.Handlers
import io.undertow.server.HttpHandler
import nl.toefel.garsson.server.WebSocketStreamContainer

fun updateStreamWebsocketHandler(webSocketsContainer: WebSocketStreamContainer): HttpHandler {
    return Handlers.websocket { exchange, webSocketChannel ->
        webSocketsContainer.addWebSocket(webSocketChannel)
        webSocketChannel.resumeReceives()
    }
}


//fun updateStreamWebsocketHandlerOld(): HttpHandler {
//    return Handlers.websocket { exchange, channel ->
//        orderUpdateSockets.add(channel)
//
//        channel.addCloseTask { channel -> orderUpdateSockets.remove(channel) }
//
//        channel.receiveSetter.set { listener ->
//            object : AbstractReceiveListener() {
//                override fun onFullTextMessage(channel: WebSocketChannel?, message: BufferedTextMessage?) {
//                    WebSockets.sendText("Hellow Undertow", channel, null)
//                }
//            }
//        }
//
//        channel.resumeReceives()
//        WebSockets.sendText("Hellow Undertow2", channel, null)
//        Thread {
//            println("SLEEPING")
//            Thread.sleep(5000)
//            println("SENDING")
//            WebSockets.sendText("Hellow Undertowz", channel, object : WebSocketCallback<Void> {
//                override fun complete(channel: WebSocketChannel?, context: Void?) {
//                    println("second sendJson complete")
//                }
//
//                override fun onError(channel: WebSocketChannel?, context: Void?, throwable: Throwable?) {
//                    println("second sendJson error")
//                    throwable?.printStackTrace()
//                }
//            })
//        }.start()
//    }
//}