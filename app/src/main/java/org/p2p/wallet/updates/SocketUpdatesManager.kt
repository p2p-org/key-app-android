package org.p2p.wallet.updates

import androidx.core.net.toUri
import com.google.gson.JsonObject
import timber.log.Timber
import java.util.concurrent.Executors
import kotlin.properties.Delegates.observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.p2p.core.network.ConnectionManager
import org.p2p.solanaj.model.types.RpcMapRequest
import org.p2p.solanaj.model.types.RpcRequest
import org.p2p.solanaj.ws.SocketClientCreateResult
import org.p2p.solanaj.ws.SocketStateListener
import org.p2p.solanaj.ws.SubscriptionEventListener
import org.p2p.solanaj.ws.SubscriptionSocketClient
import org.p2p.solanaj.ws.SubscriptionSocketClientFactory
import org.p2p.solanaj.ws.impl.SocketClientException
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.common.feature_toggles.toggles.remote.SocketSubscriptionsFeatureToggle
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironment
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager

private const val DELAY_MS = 5000L

private const val TAG = "Sockets:SocketUpdatesManager"

class SocketUpdatesManager(
    appScope: AppScope,
    private val environmentManager: NetworkEnvironmentManager,
    private val connectionStateProvider: ConnectionManager,
    private val updateHandlers: List<SubscriptionUpdateHandler>,
    private val socketEnabledFeatureToggle: SocketSubscriptionsFeatureToggle,
    private val initDispatcher: CoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
) : CoroutineScope by (appScope + initDispatcher), SocketStateListener, SubscriptionUpdatesManager {

    private val networkEnvironment: NetworkEnvironment
        get() = environmentManager.loadCurrentEnvironment()
    private var client: SubscriptionSocketClient? = null
    private var connectionJob: Job? = null

    private val observers = mutableListOf<SubscriptionUpdatesStateObserver>()

    private var state: SocketState by observable(SocketState.DISCONNECTED) { _, oldValue, newValue ->
        if (oldValue != newValue) observers.lastOrNull()?.onUpdatesStateChanged(newValue)
    }

    private val socketFactory = SubscriptionSocketClientFactory()

    private var isStarted = false

    override fun start() {
        if (isStarted || !socketEnabledFeatureToggle.isFeatureEnabled) {
            Timber.tag(TAG).i(
                "Unable to start socket manager: isStarted=$isStarted flag=${socketEnabledFeatureToggle.value}"
            )
            return
        }

        Timber.tag(TAG).i("Starting update manager")
        launch { startActual() }
    }

    override fun stop() {
        launch(NonCancellable) { stopActual() }
    }

    override suspend fun restart() {
        withContext(initDispatcher) { stopActual() }
        start()
    }

    override fun addUpdatesStateObserver(observer: SubscriptionUpdatesStateObserver) {
        launch {
            observer.onUpdatesStateChanged(state)
            observers.add(observer)
        }
    }

    override fun removeUpdatesStateObserver(observer: SubscriptionUpdatesStateObserver) {
        launch { observers.remove(observer) }
    }

    override fun addSubscription(request: RpcRequest, updateType: SocketSubscriptionUpdateType) {
        Timber.tag(TAG).d("add subscription for request, client = $client")
        val listener = createEventListener(updateType)
        client?.addSubscription(request, listener)
    }

    override fun addSubscription(request: RpcMapRequest, updateType: SocketSubscriptionUpdateType) {
        Timber.tag(TAG).d("add subscription for request, client = $client")
        val listener = createEventListener(updateType)
        client?.addSubscription(request, listener)
    }

    private fun createEventListener(updateType: SocketSubscriptionUpdateType): SubscriptionEventListener =
        SubscriptionEventListener { data: JsonObject ->
            launch(Dispatchers.Default) {
                Timber.tag(TAG).d("New socket event triggered($updateType): $data")

                updateHandlers.forEach {
                    it.onUpdate(updateType, data)
                }
            }
        }

    override fun removeSubscription(request: RpcRequest) {
        client?.removeSubscription(request)
    }

    override fun removeSubscription(request: RpcMapRequest) {
        client?.removeSubscription(request)
    }

    override fun onWebSocketPong() {
        Timber.tag(TAG).d("Server pong")
    }

    override fun onConnected() {
        Timber.tag(TAG).i("Socket client is successfully connected")
        connectionJob?.cancel()
        connectionJob = launch {
            state = SocketState.CONNECTED
            while (true) {
                client?.ping()
                delay(DELAY_MS)
            }
        }
    }

    override fun onFailed(exception: Exception) {
        Timber.tag(TAG).d("Event updates connection is failed: $exception")
        if (state == SocketState.CONNECTING) {
            launch { state = SocketState.CONNECTING_FAILED }
        } else {
            restartInternal()
        }
    }

    override fun onClosed(code: Int, message: String) {
        Timber.tag(TAG).i("Event updates connection is closed: $message")
    }

    private suspend fun startActual() {
        isStarted = true

        when (state) {
            SocketState.DISCONNECTED,
            SocketState.INITIALIZATION_FAILED,
            SocketState.CONNECTING_FAILED ->
                connectSocketClient()
            SocketState.INITIALIZING ->
                Timber.tag(TAG).i("Trying to start update manager while it is initializing")
            SocketState.CONNECTING ->
                Timber.tag(TAG).i("Trying to start update manager while it is connecting")
            SocketState.CONNECTED ->
                Timber.tag(TAG).i("Trying to start update manager while it is connected")
        }
    }

    private fun stopActual() {
        if (!isStarted) {
            Timber.tag(TAG).i("Trying to stop update manager while it is already stopped")
        } else {
            isStarted = false
            Timber.tag(TAG).i("Stopping update manager")
            disconnectSocketClient()
        }
    }

    private suspend fun connectSocketClient() {
        if (state == SocketState.CONNECTED) {
            Timber.tag(TAG).i("Trying to connect update manager while it is already connected")
            return
        }

        state = SocketState.INITIALIZING
        Timber.tag(TAG).i("Connecting: Waiting for network")

        try {
            withTimeout(DELAY_MS) { connectionStateProvider.connectionStatus.value }
            Timber.tag(TAG).i("Connecting: Network OK, initializing update handlers")
            updateHandlers.forEach { it.initialize() }
        } catch (e: Throwable) {
            Timber.tag(TAG).i(SocketClientException(e), "Awaiting network failed")
            state = SocketState.INITIALIZATION_FAILED
            return
        }

        state = SocketState.CONNECTING
        Timber.tag(TAG).i("Connecting: Update handlers are initialized, connecting to event stream")

        try {
            val validatedEndpoint = createValidatedEndpoint()
            when (val result = socketFactory.create(validatedEndpoint, this)) {
                is SocketClientCreateResult.Created -> {
                    client = result.instance
                    if (client?.isSocketOpen == false) {
                        client?.connect()
                        Timber.tag(TAG).i("Connection created for : $client")
                    }
                    state = SocketState.CONNECTED
                }
                is SocketClientCreateResult.Reused -> {
                    client = result.instance
                    client?.reconnect()
                    Timber.tag(TAG).i("Connection reused for : $client")
                    state = SocketState.CONNECTED
                }
                is SocketClientCreateResult.Failed -> throw result
            }
        } catch (e: Throwable) {
            Timber.tag(TAG).e(SocketClientException(e), "Connecting failed")
            state = SocketState.CONNECTING_FAILED
        }
    }

    private fun createValidatedEndpoint(): String {
        val endpoint = networkEnvironment.endpoint
        return if (networkEnvironment == NetworkEnvironment.RPC_POOL) {
            endpoint.toUri()
                .buildUpon()
                .appendEncodedPath(BuildConfig.rpcPoolApiKey)
                .toString()
        } else {
            endpoint
        }
    }

    private fun disconnectSocketClient() {
        client?.close()
        state = SocketState.DISCONNECTED
        Timber.tag(TAG).i("Disconnected from socket client")
    }

    private fun restartInternal() {
        if (!isStarted) {
            Timber.tag(TAG).i("Stopped")
        } else {
            Timber.tag(TAG).i("Reconnecting")
            launch {
                delay(DELAY_MS)
                client?.reconnect()
            }
        }
    }
}
