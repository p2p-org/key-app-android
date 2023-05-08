package org.p2p.solanaj.ws

import com.google.gson.Gson
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.framing.Framedata
import org.java_websocket.handshake.ServerHandshake
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.net.URI
import java.net.URISyntaxException
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcNotificationResponse
import org.p2p.solanaj.model.types.RpcRequest

private const val TAG = "Sockets:SubscriptionSocketClient"

class SubscriptionWebSocketClient private constructor(serverURI: URI?) : WebSocketClient(serverURI), KoinComponent {

    companion object {
        private var instance: SubscriptionWebSocketClient? = null
        private var socketStateListener: SocketStateListener? = null

        fun getInstance(
            endpoint: String,
            stateListener: SocketStateListener?
        ): SubscriptionWebSocketClient? {
            val endpointURI: URI
            val serverURI: URI
            socketStateListener = stateListener
            try {
                endpointURI = URI(endpoint)
                val scheme = if (endpointURI.scheme === "https") "wss" else "ws"
                serverURI = URI("$scheme://${endpointURI.host}${endpointURI.path}")
            } catch (e: URISyntaxException) {
                Timber.tag(TAG).i(e)
                throw IllegalArgumentException(e)
            }
            Timber.tag(TAG).d("Creating connection, uri: $serverURI + host: $endpointURI")
            try {
                if (instance == null) {
                    instance = SubscriptionWebSocketClient(serverURI)
                }
                Timber.tag(TAG).i("Web socket client is created for : $serverURI")
            } catch (e: Throwable) {
                Timber.tag(TAG).e("Error on creating web socket client, error = $e")
            }

            if (instance?.isOpen == false) {
                instance?.connect()
                Timber.tag(TAG).i("Connection created for : $serverURI")
            }
            return instance
        }
    }

    private val gson: Gson by inject()
    private val subscriptions = mutableMapOf<String, SubscriptionParams<*>?>()
    private val requestsIdsToSubscriptionIds = mutableMapOf<String, Long?>()
    private val subscriptionListeners = mutableMapOf<Long, NotificationEventListener>()

    fun ping() {
        try {
            if (instance?.isOpen == true) instance?.sendPing()
            Timber.tag(TAG).d("Server PING")
        } catch (error: WebsocketNotConnectedException) {
            Timber.tag(TAG).e(error, "Error on ping socket")
        }
    }

    fun addSubscription(request: RpcMapRequest, listener: NotificationEventListener) {
        Timber.tag(TAG).i("Add subscription for request = $request")
        subscriptions[request.id] = SubscriptionParams(request, listener)
        requestsIdsToSubscriptionIds[request.id] = null
        updateSubscriptions()
    }

    fun addSubscription(request: RpcRequest, listener: NotificationEventListener) {
        Timber.tag(TAG).i("Add subscription for request = $request")
        subscriptions[request.id] = SubscriptionParams(request, listener)
        requestsIdsToSubscriptionIds[request.id] = null
        updateSubscriptions()
    }

    fun removeSubscription(request: RpcMapRequest) {
        send(gson.toJson(request))

        subscriptions[request.id] = null
        requestsIdsToSubscriptionIds[request.id] = null

        updateSubscriptions()
    }

    fun removeSubscription(request: RpcRequest) {
        send(gson.toJson(request))

        subscriptions[request.id] = null
        requestsIdsToSubscriptionIds[request.id] = null

        updateSubscriptions()
    }

    override fun onWebsocketPong(socket: WebSocket, framedata: Framedata) {
        super.onWebsocketPong(socket, framedata)
        socketStateListener?.onWebSocketPong()
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        socketStateListener?.onConnected()
        updateSubscriptions()
    }

    override fun onMessage(message: String) {
        Timber.tag(TAG).d("New message received: %s", message)
        try {
            val response = gson.fromJson(message, RpcNotificationResponse::class.java)
            val requestId = response?.id
            val subscriptionId = response?.subscriptionId
            if (requestId != null && subscriptionId != null) {
                handleSubscribeResponse(requestId, subscriptionId)
            } else {
                handleSubscriptionNewUpdate(response)
            }
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error on reading message")
        }
    }

    private fun handleSubscriptionNewUpdate(response: RpcNotificationResponse?){
        response?.params ?: return
        val subscriptionId = response.params.get("subscription").asInt.toLong()
        val listener = subscriptionListeners[subscriptionId]
        listener?.onNotificationEvent(response.params)

        val logString = buildString {
            append("Find listener for subscription = $subscriptionId in")
            append(" ${subscriptionListeners.keys.map(Long::toString)}")
        }
        Timber.tag(TAG).d(logString)
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        val closedFrom = if (remote) "remote peer" else "us"
        socketStateListener?.onClosed(
            code = code,
            message = "Connection closed by $closedFrom Code: $code Reason: $reason"
        )
    }

    override fun onError(ex: Exception) {
        Timber.tag(TAG).e(ex, "Error on socket working")
        socketStateListener?.onFailed(ex)
    }

    private fun updateSubscriptions() {
        if (instance?.isOpen == true) {
            for (sub in subscriptions.values) {
                when (sub?.request) {
                    is RpcRequest -> {
                        val requestJson = gson.toJson(sub.request as RpcRequest)
                        send(requestJson)
                        Timber.tag(TAG).d("Add subscription for request = $requestJson")
                    }
                    is RpcMapRequest -> {
                        val requestJson = gson.toJson(sub.request as RpcMapRequest)
                        send(requestJson)
                        Timber.tag(TAG).d("Add subscription for request = $requestJson")
                    }
                }
            }
        }
    }

    private fun handleSubscribeResponse(requestId: String, subscriptionId: Long) {
        Timber.tag(TAG).d("Add subscription listeners, ${subscriptionListeners.keys.map { it.toString() }}")
        if (requestId in requestsIdsToSubscriptionIds) {
            requestsIdsToSubscriptionIds[requestId] = subscriptionId
            subscriptions[requestId]?.listener?.also { listener ->
                subscriptionListeners[subscriptionId] = listener
            }
            subscriptions.remove(requestId)
        }
    }

    private class SubscriptionParams<RpcRequest>(
        val request: RpcRequest,
        val listener: NotificationEventListener
    )
}
