package org.p2p.solanaj.ws.impl

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
import java.net.UnknownHostException
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcNotificationResponse
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.ws.SocketStateListener
import org.p2p.solanaj.ws.SubscriptionEventListener
import org.p2p.solanaj.ws.SubscriptionSocketClient

private const val TAG = "SubscriptionSocketClient"
private typealias SubscriptionId = Long

class SocketClientException(
    override val cause: Throwable,
    override val message: String? = cause.message,
): Throwable()

internal class SubscriptionWebSocketClient internal constructor(
    serverURI: URI,
    private val stateListener: SocketStateListener,
) : WebSocketClient(serverURI), KoinComponent, SubscriptionSocketClient {

    private class SubscriptionParams<RpcRequest>(
        val request: RpcRequest,
        val listener: SubscriptionEventListener
    )

    private val gson: Gson by inject()
    private val subscriptions = mutableMapOf<String, SubscriptionParams<*>?>()
    private val requestsIdsToSubscriptionIds = mutableMapOf<String, SubscriptionId?>()
    private val subscriptionListeners = mutableMapOf<SubscriptionId, SubscriptionEventListener>()

    override val isSocketOpen: Boolean
        get() = super.isOpen()

    override fun ping() {
        try {
            if (isSocketOpen) {
                Timber.tag(TAG).d("Server PING")
                sendPing()
            }
        } catch (error: WebsocketNotConnectedException) {
            Timber.tag(TAG).e(SocketClientException(error), "Error on ping socket")
        }
    }

    override fun addSubscription(request: RpcMapRequest, listener: SubscriptionEventListener) {
        Timber.tag(TAG).i("addSubscription(RpcMapRequest) = $request")

        subscriptions[request.id] = SubscriptionParams(request, listener)
        requestsIdsToSubscriptionIds[request.id] = null
        updateSubscriptions()
    }

    override fun addSubscription(request: RpcRequest, listener: SubscriptionEventListener) {
        Timber.tag(TAG).i("addSubscription(RpcRequest) = $request")

        subscriptions[request.id] = SubscriptionParams(request, listener)
        requestsIdsToSubscriptionIds[request.id] = null
        updateSubscriptions()
    }

    override fun removeSubscription(request: RpcMapRequest) {
        send(gson.toJson(request))

        subscriptions[request.id] = null
        requestsIdsToSubscriptionIds[request.id] = null

        updateSubscriptions()
    }

    override fun removeSubscription(request: RpcRequest) {
        send(gson.toJson(request))

        subscriptions[request.id] = null
        requestsIdsToSubscriptionIds[request.id] = null

        updateSubscriptions()
    }

    override fun onWebsocketPong(socket: WebSocket, framedata: Framedata) {
        super.onWebsocketPong(socket, framedata)
        stateListener.onWebSocketPong()
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        stateListener.onConnected()
        updateSubscriptions()
    }

    override fun onMessage(message: String) {
        Timber.tag(TAG).d("New message received: $message")
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
            Timber.tag(TAG).e(SocketClientException(e), "Error on reading message, $message")
        }
    }

    private fun handleSubscriptionNewUpdate(response: RpcNotificationResponse?) {
        response?.params ?: return
        val subscriptionId = response.params.get("subscription").asInt.toLong()
        val listener = subscriptionListeners[subscriptionId]
        listener?.onSubscriptionUpdated(response.params)

        val logString = buildString {
            append("Find listener for subscription = $subscriptionId in ")
            append("${subscriptionListeners.keys.map(Long::toString)}")
        }
        Timber.tag(TAG).d(logString)
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        val closedFrom = if (remote) "remote peer" else "us"
        stateListener.onClosed(
            code = code,
            message = "Connection closed by $closedFrom Code: $code Reason: $reason"
        )
    }

    override fun onError(ex: Exception) {
        val logger = Timber.tag(TAG)
        if(ex is UnknownHostException) {
            logger.w(SocketClientException(ex), "Error on socket working")
        } else {
            logger.e(SocketClientException(ex), "Error on socket working")
        }

        stateListener.onFailed(ex)
    }

    private fun updateSubscriptions() {
        if (isSocketOpen) {
            for (sub in subscriptions.values.filterNotNull()) {
                val requestJson = when (sub.request) {
                    is RpcRequest -> gson.toJson(sub.request)
                    is RpcMapRequest -> gson.toJson(sub.request)
                    else -> return
                }

                send(requestJson)
                Timber.tag(TAG).d("making subscription request = $requestJson")
            }
        }
    }

    private fun handleSubscribeResponse(requestId: String, subscriptionId: SubscriptionId) {
        Timber.tag(TAG).d("handleSubscribeResponse: current=${subscriptionListeners.keys.joinToString()}")
        if (requestId in requestsIdsToSubscriptionIds) {
            requestsIdsToSubscriptionIds[requestId] = subscriptionId
            subscriptions[requestId]?.listener?.also { listener ->
                subscriptionListeners[subscriptionId] = listener
            }
            subscriptions.remove(requestId)
        }
        Timber.tag(TAG).d("handleSubscribeResponse: after=${subscriptionListeners.keys.joinToString()}")
    }
}
