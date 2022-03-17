package org.p2p.solanaj.ws

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.framing.Framedata
import org.java_websocket.handshake.ServerHandshake
import org.p2p.solanaj.model.types.RpcNotificationResult
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.model.types.RpcResponse
import java.net.URI
import java.net.URISyntaxException

// TODO: Refactor this class, move to kotlin, handle 1006 error, maybe ping/pong issue
class SubscriptionWebSocketClient(serverURI: URI?) : WebSocketClient(serverURI) {
    private inner class SubscriptionParams constructor(
        var request: RpcRequest,
        var listener: NotificationEventListener
    )

    private val subscriptions = mutableMapOf<String, SubscriptionParams>()
    private val subscriptionIds = mutableMapOf<String, Long?>()
    private val subscriptionListeners = mutableMapOf<Long, NotificationEventListener>()
    fun ping() {
        try {
            if (instance?.isOpen == true) instance?.sendPing()
        } catch (error: WebsocketNotConnectedException) {
            Log.e("SOCKET", "Error on ping socket", error)
        }
    }

    fun accountSubscribe(key: String, listener: NotificationEventListener) {
        val rpcRequest = RpcRequest("accountSubscribe", mutableListOf(key))
        subscriptions[rpcRequest.id] = SubscriptionParams(rpcRequest, listener)
        subscriptionIds[rpcRequest.id] = null
        updateSubscriptions()
    }

    fun signatureSubscribe(signature: String, listener: NotificationEventListener) {
        val rpcRequest = RpcRequest("signatureSubscribe", mutableListOf(signature))
        subscriptions[rpcRequest.id] = SubscriptionParams(rpcRequest, listener)
        subscriptionIds[rpcRequest.id] = null
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
        Log.d("SOCKET", "New message received: $message")
        val resultAdapter = Moshi.Builder().build()
            .adapter<RpcResponse<Long>>(Types.newParameterizedType(RpcResponse::class.java, Long::class.java))
        try {
            resultAdapter.fromJson(message)?.also { rpcResult ->
                val rpcResultId = rpcResult.id
                if (rpcResultId != null) {
                    if (subscriptionIds.containsKey(rpcResultId)) {
                        subscriptionIds[rpcResultId] = rpcResult.result
                        subscriptions[rpcResultId]?.listener?.let { listener ->
                            subscriptionListeners[rpcResult.result] = listener
                        }
                        subscriptions.remove(rpcResultId)
                    }
                } else {
                    val notificationResultAdapter = Moshi.Builder().build().adapter(RpcNotificationResult::class.java)
                    notificationResultAdapter.fromJson(message)?.let { result ->
                        val listener = subscriptionListeners[result.params.subscription]
                        val value = result.params.result.value as Map<*, *>
                        when (result.method) {
                            "signatureNotification" -> listener!!.onNotificationEvent(
                                SignatureNotification(
                                    value["err"]
                                )
                            )
                            "accountNotification" -> listener!!.onNotificationEvent(value)
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("SOCKET", "Error on socket message", ex)
        }
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        socketStateListener?.onClosed(
            code,
            "Connection closed by "
                + (if (remote) "remote peer" else "us")
                + " Code: " + code + " Reason: " + reason
        )
    }

    override fun onError(ex: Exception) {
        Log.e("SOCKET", "Error on socket working", ex)
        socketStateListener?.onFailed(ex)
    }

    private fun updateSubscriptions() {
        if (isOpen && subscriptions.isNotEmpty()) {
            val rpcRequestJsonAdapter = Moshi.Builder().build().adapter(
                RpcRequest::class.java
            )
            for (sub in subscriptions.values) {
                send(rpcRequestJsonAdapter.toJson(sub.request))
            }
        }
    }

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
            Log.d("SOCKET", "Creating connection, uri: " + serverURI + " + host: " + endpointURI.host)
            if (instance == null) {
                instance = SubscriptionWebSocketClient(serverURI)
            }
            if (instance?.isOpen == false) {
                instance?.connect()
            }
            return instance
        }
    }
}