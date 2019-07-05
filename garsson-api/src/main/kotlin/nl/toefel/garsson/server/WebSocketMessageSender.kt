package nl.toefel.garsson.server

import io.undertow.websockets.core.WebSocketCallback
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import nl.toefel.garsson.dto.ResourceUpdatedEvent
import nl.toefel.garsson.dto.UpdateType
import nl.toefel.garsson.dto.User
import nl.toefel.garsson.json.Jsonizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Message sender that broadcasts a message to all open WebSockets
 */
class WebSocketMessageSender(val socketStreamContainer: WebSocketStreamContainer) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(WebSocketMessageSender::class.java)
    }

    fun sendJson(message: Any) {
        val messageJson = Jsonizer.toJson(message)
        sendString(messageJson)
    }

    fun sendString(message: String) {
        val messageJson = Jsonizer.toJson(message)
        logger.info("Sending message to all open WebSockets $messageJson")

        val openSockets = socketStreamContainer.getAllOpenWebSockets()

        openSockets.forEach {
            safeSendMessage(it, messageJson)
        }
    }

    fun resourceAdded(resourceName: String, resourceId: String, causedBy: String) {
        val event = ResourceUpdatedEvent(resourceName, resourceId, UpdateType.ADDED, causedBy)
        sendJson(event)
    }

    fun resourceDeleted(resourceName: String, resourceId: String, causedBy: String) {
        val event = ResourceUpdatedEvent(resourceName, resourceId, UpdateType.DELETED, causedBy)
        sendJson(event)
    }

    fun resourceUpdated(resourceName: String, resourceId: String, causedBy: String) {
        val event = ResourceUpdatedEvent(resourceName, resourceId, UpdateType.UPDATED, causedBy)
        sendJson(event)
    }

    private fun safeSendMessage(webSocket: WebSocketChannel, event: String) {
        try {
            WebSockets.sendText(event, webSocket, object : WebSocketCallback<Void> {
                override fun complete(channel: WebSocketChannel?, context: Void?) {
                    logger.debug("successfully sent WebSocket message to ${channel?.peerAddress}: event")
                }

                override fun onError(channel: WebSocketChannel?, context: Void?, throwable: Throwable?) {
                    logger.warn("error while sending WebSocket message to ${channel?.peerAddress}: ${throwable?.message}", throwable)
                }
            })
        } catch (ex: Exception) {
            logger.warn("error while sending WebSocket message to ${webSocket.peerAddress}: ${ex.message}", ex)
        }
    }

}