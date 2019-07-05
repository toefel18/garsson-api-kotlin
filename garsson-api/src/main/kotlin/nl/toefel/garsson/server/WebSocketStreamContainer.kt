package nl.toefel.garsson.server

import io.undertow.websockets.core.WebSocketChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Threadsafe container for all WebSocket connections. Listens for close events and remove them.
 */
class WebSocketStreamContainer {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(WebSocketStreamContainer::class.java)
    }

    private val openWebSockets = mutableSetOf<WebSocketChannel>()
    private val rwLock = ReentrantReadWriteLock()

    /**
     * Adds a WebSocket to the container and listen for its close event to auto-remove them.
     */
    fun addWebSocket(channel: WebSocketChannel) {
        logger.info("adding new WebSocket channel to peer ${channel.peerAddress}")
        rwLock.writeLock().lock()
        try {
            if (!channel.isOpen) {
                logger.warn("trying to add an already closed WebSocket channel is already closed")
                openWebSockets.remove(channel)
            } else {
                openWebSockets.add(channel)
                channel.addCloseTask { closedChannel -> removeClosed(closedChannel) }
            }
        } finally {
            rwLock.writeLock().unlock()
        }
    }

    /**
     * Returns a copy of all open WebSockets
     */
    fun getAllOpenWebSockets(): Set<WebSocketChannel> {
        return copyOfOpenWebSockets()
    }

    /**
     * Closes all open WebSockets and logs on error.
     */
    fun closeAllSockets() {
        logger.info("closing all WebSockets")
        val allSockets = copyOfOpenWebSockets()

        allSockets.forEach { channel ->
            closeSocket(channel)
        }
    }

    private fun closeSocket(channel: WebSocketChannel) {
        logger.info("closing WebSocket to peer ${channel.peerAddress}")
        try {
            channel.close()
        } catch (e: Exception) {
            logger.warn("exception while closing WebSocket to ${channel.peerAddress}", e)
        }
    }

    private fun copyOfOpenWebSockets(): HashSet<WebSocketChannel> {
        rwLock.readLock().lock()
        val allSockets = try {
            HashSet(openWebSockets)
        } finally {
            rwLock.readLock().unlock()
        }
        return allSockets
    }

    private fun removeClosed(closedChannel: WebSocketChannel) {
        logger.info("removing closed WebSocket to peer ${closedChannel.peerAddress}")
        rwLock.writeLock().lock()
        try {
            openWebSockets.remove(closedChannel)
        } finally {
            rwLock.writeLock().unlock()
        }
    }
}