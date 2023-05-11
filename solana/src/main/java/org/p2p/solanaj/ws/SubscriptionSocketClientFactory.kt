package org.p2p.solanaj.ws

import timber.log.Timber
import java.net.URI
import org.p2p.solanaj.ws.impl.SocketClientException
import org.p2p.solanaj.ws.impl.SubscriptionWebSocketClient

private const val TAG = "Sockets:SubscriptionWebSocketClientFactory"

class SubscriptionSocketClientFactory {
    companion object {
        private var instance: SubscriptionWebSocketClient? = null

        private const val WSS_SCHEME = "wss"
        private const val WS_SCHEME = "ws"
        private const val HTTPS_SCHEME = "https"
    }

    fun create(serverUri: String, stateListener: SocketStateListener): SocketClientCreateResult = try {
        if (instance != null) {
            Timber.i("Reusing already created client: $instance")
            SocketClientCreateResult.Created(instance!!)
        } else {
            val endpointUri = URI(serverUri)
            val socketUri = buildSocketUri(endpointUri)
            Timber.tag(TAG).d("Creating connection, uri: $socketUri + host: $endpointUri")

            instance = SubscriptionWebSocketClient(socketUri, stateListener)
            Timber.tag(TAG).i("Web socket client is created for : $socketUri")

            SocketClientCreateResult.Created(instance!!)
        }
    } catch (creationError: Throwable) {
        Timber.e(SocketClientException(creationError), "failed to create")
        SocketClientCreateResult.Failed(creationError)
    }

    private fun buildSocketUri(endpointUri: URI): URI {
        val scheme = if (endpointUri.scheme == HTTPS_SCHEME) WSS_SCHEME else WS_SCHEME
        return URI("$scheme://${endpointUri.host}${endpointUri.path}")
    }
}
