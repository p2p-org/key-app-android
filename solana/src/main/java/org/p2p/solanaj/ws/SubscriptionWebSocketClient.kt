package org.p2p.solanaj.ws

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.framing.Framedata
import org.java_websocket.handshake.ServerHandshake
import timber.log.Timber
import java.net.URI
import java.net.URISyntaxException
import org.p2p.solanaj.model.types.*
import org.p2p.solanaj.utils.MoshiUtils

private const val TAG = "SubscriptionSocketClient"

class SubscriptionWebSocketClient(serverURI: URI?) : WebSocketClient(serverURI) {

    companion object {
        private var instance: SubscriptionWebSocketClient? = null
        private var socketStateListener: SocketStateListener? = null

        fun getInstance(endpoint: String?, stateListener: SocketStateListener?): SubscriptionWebSocketClient? {
            val endpointURI: URI
            val serverURI: URI
            socketStateListener = stateListener
            try {
                endpointURI = URI(endpoint)
                serverURI = URI(if (endpointURI.scheme === "https") "wss" else "ws" + "://" + endpointURI.host)
            } catch (e: URISyntaxException) {
                throw IllegalArgumentException(e)
            }
            Timber.tag(TAG).d("Creating connection, uri: " + serverURI + " + host: " + endpointURI.host)
            try {
                if (instance == null) {
                    instance = SubscriptionWebSocketClient(serverURI)
                }
                Timber.tag(TAG).d("Web socket client is created for : $serverURI")
            } catch (e: Throwable) {
                Timber.tag(TAG).e("Error on creating web socket client, error = $e")
            }

            if (instance?.isOpen == false) {
                instance?.connect()
                Timber.tag(TAG).d("Connection created for : $serverURI")
            }
            return instance
        }
    }

    private val subscriptions = mutableMapOf<String, SubscriptionParams<*>?>()
    private val subscriptionIds = mutableMapOf<String, Long?>()
    private val subscriptionListeners = mutableMapOf<Long, NotificationEventListener>()

    private val moshi = Moshi.Builder().build()
    private val listGsonAdapter: JsonAdapter<RpcRequest> = moshi.adapter(RpcRequest::class.java)
    private val mapGsonAdapter: JsonAdapter<RpcMapRequest> = moshi.adapter(RpcMapRequest::class.java)
    private val rpcNotificationAdapter: JsonAdapter<RpcNotificationResponse> =
        moshi.adapter(RpcNotificationResponse::class.java)
    private val rpcSubscriptionAdapter = moshi.adapter<RpcResponse<Long>>(
        Types.newParameterizedType(
            RpcResponse::class.java,
            Long::class.javaObjectType
        )
    )

    fun ping() {
        try {
            if (instance?.isOpen == true) instance?.sendPing()
            Timber.tag(TAG).d("Server PING")
        } catch (error: WebsocketNotConnectedException) {
            Timber.tag(TAG).e(error, "Error on ping socket")
        }
    }

    fun addSubscription(request: RpcMapRequest, listener: NotificationEventListener) {
        Timber.tag(TAG).d("Add subscription for request = $request")
        subscriptions[request.id] = SubscriptionParams(request, listener)
        subscriptionIds[request.id] = null
        updateSubscriptions()
    }

    fun addSubscription(request: RpcRequest, listener: NotificationEventListener) {
        Timber.tag(TAG).d("Add subscription for request = $request")
        subscriptions[request.id] = SubscriptionParams(request, listener)
        subscriptionIds[request.id] = null
        updateSubscriptions()
    }

    fun removeSubscription(request: RpcMapRequest) {
        send(mapGsonAdapter.toJson(request))

        subscriptions[request.id] = null
        subscriptionIds[request.id] = null

        updateSubscriptions()
    }

    fun removeSubscription(request: RpcRequest) {
        send(listGsonAdapter.toJson(request))

        subscriptions[request.id] = null
        subscriptionIds[request.id] = null

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
            if (MoshiUtils.canParse(message, rpcSubscriptionAdapter)) {
                val subscriptionResult = rpcSubscriptionAdapter.fromJson(message)
                val resultId = subscriptionResult?.id ?: return
                addResultAndNotify(resultId, subscriptionResult)
                Timber.tag(TAG).d("Add subscription listeners, ${subscriptionListeners.keys.map { it.toString() }}")
            } else {
                val eventResult = rpcNotificationAdapter.fromJson(message)
                val listener = subscriptionListeners[eventResult?.subscription]
                listener?.onNotificationEvent(eventResult?.params)
                Timber.tag(TAG)
                    .d("Find listener for subscription = ${eventResult?.subscription} in  ${subscriptionListeners.keys.map { it.toString() }}")
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error on reading message")
        }
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        val closedFrom = if (remote) "remote peer" else "us"
        socketStateListener?.onClosed(
            code,
            "Connection closed by $closedFrom Code: $code Reason: $reason"
        )
    }

    override fun onError(ex: Exception) {
        Timber.tag(TAG).e(ex, "Error on socket working")
        socketStateListener?.onFailed(ex)
    }

    private fun updateSubscriptions() {
        if (instance?.isOpen == true && subscriptions.isNotEmpty()) {
            for (sub in subscriptions.values) {
                when (sub?.request) {
                    is RpcRequest -> {
                        val requestJson = listGsonAdapter.toJson(sub.request as RpcRequest)
                        send(requestJson)
                        Timber.tag(TAG).d("Add subscription for request = $requestJson")
                    }
                    is RpcMapRequest -> {
                        val requestJson = mapGsonAdapter.toJson(sub.request as RpcMapRequest)
                        send(requestJson)
                        Timber.tag(TAG).d("Add subscription for request = $requestJson")
                    }
                }
            }
        }
    }

    private fun addResultAndNotify(resultId: String, rpcResult: RpcResponse<Long>) {
        if (subscriptionIds.containsKey(resultId)) {
            subscriptionIds[resultId] = rpcResult.result
            subscriptions[resultId]?.listener?.let { listener ->
                subscriptionListeners[rpcResult.result] = listener
            }
            subscriptions.remove(resultId)
        }
    }

    private class SubscriptionParams<RpcRequest>(
        var request: RpcRequest,
        var listener: NotificationEventListener
    )

    enum class NotificationType(val type: String) {
        SIGNATURE("signatureNotification"),
        ACCOUNT("accountNotification");

        companion object {
            fun valueOf(type: String?): NotificationType? {
                return values().firstOrNull { it.type == type }
            }
        }
    }
}
