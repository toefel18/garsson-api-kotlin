package nl.toefel.garsson.server.handlers

import io.undertow.Handlers
import io.undertow.server.HttpHandler
import nl.toefel.garsson.server.WebSocketStreamContainer

fun updateStreamWebSocketHandler(webSocketsContainer: WebSocketStreamContainer): HttpHandler {
    return Handlers.websocket { _, webSocketChannel ->
        webSocketsContainer.addWebSocket(webSocketChannel)
        webSocketChannel.resumeReceives()
    }
}
