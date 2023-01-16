package org.p2p.wallet.updates

import org.p2p.solanaj.ws.SocketStateListener
import org.p2p.solanaj.ws.SubscriptionWebSocketClient
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import timber.log.Timber
import java.util.concurrent.Executors
import kotlin.properties.Delegates
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

private const val DELAY_MS = 5000L

private const val TAG = "SOCKET"

class SocketUpdatesManager private constructor(
    appScope: AppScope,
    environmentManager: NetworkEnvironmentManager,
    private val connectionStateProvider: ConnectionStateProvider,
    private val updateHandlers: List<UpdateHandler>,
    private val initDispatcher: CoroutineDispatcher
) : CoroutineScope by (appScope + initDispatcher), SocketStateListener, UpdatesManager {

    constructor(
        appScope: AppScope,
        environmentManager: NetworkEnvironmentManager,
        connectionStateProvider: ConnectionStateProvider,
        updateHandlers: List<UpdateHandler>
    ) : this(
        appScope = appScope,
        environmentManager = environmentManager,
        connectionStateProvider = connectionStateProvider,
        updateHandlers = updateHandlers,
        initDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )

    private val endpoint = environmentManager.loadCurrentEnvironment().endpoint
    private var client: SubscriptionWebSocketClient? = null
    private var connectionJob: Job? = null

    private val observers = mutableListOf<UpdatesStateObserver>()

    private var state: UpdatesState by Delegates.observable(UpdatesState.DISCONNECTED) { _, oldValue, newValue ->
        if (oldValue != newValue) observers.lastOrNull()?.onUpdatesStateChanged(newValue)
    }

    private var isStarted = false

    override fun start() {
        if (isStarted) return

        launch { startActual() }
    }

    override fun stop() {
        launch(NonCancellable) { stopActual() }
    }

    override suspend fun restart() {
        withContext(initDispatcher) { stopActual() }
        start()
    }

    override fun addUpdatesStateObserver(observer: UpdatesStateObserver) {
        launch {
            observer.onUpdatesStateChanged(state)
            observers.add(observer)
        }
    }

    override fun removeUpdatesStateObserver(observer: UpdatesStateObserver) {
        launch { observers.remove(observer) }
    }

    override fun subscribeToTransaction(signature: String) {
        client?.signatureSubscribe(signature) { data ->
            launch(Dispatchers.Default) {
                Timber.tag(TAG).d("Event received, data = $data")
                updateHandlers.forEach {
                    it.onUpdate(UpdateType.SIGNATURE_RECEIVED, signature)
                }
            }
        }
    }

    override fun unsubscribeFromTransaction(signature: String) = Unit

    override fun onWebSocketPong() = Unit

    override fun onConnected() {
        Timber.tag(TAG).w("Socket client is successfully connected")
        connectionJob?.cancel()
        connectionJob = launch {
            state = UpdatesState.CONNECTED
            while (true) {
                client?.ping()
                delay(DELAY_MS)
            }
        }
    }

    override fun onFailed(exception: Exception) {
        if (state == UpdatesState.CONNECTING) {
            launch { state = UpdatesState.CONNECTING_FAILED }
        } else {
            restartInternal()
        }
    }

    override fun onClosed(code: Int, message: String) {
        Timber.tag(TAG).d("Event updates connection is closed: $message")
    }

    private suspend fun startActual() {
        isStarted = true

        when (state) {
            UpdatesState.DISCONNECTED,
            UpdatesState.INITIALIZATION_FAILED,
            UpdatesState.CONNECTING_FAILED ->
                connectSocketClient()
            UpdatesState.INITIALIZING ->
                Timber.tag(TAG).d("Trying to start update manager while it is initializing")
            UpdatesState.CONNECTING ->
                Timber.tag(TAG).d("Trying to start update manager while it is connecting")
            UpdatesState.CONNECTED ->
                Timber.tag(TAG).d("Trying to start update manager while it is connected")
        }
    }

    private fun stopActual() {
        if (!isStarted) {
            Timber.tag(TAG).w("Trying to stop update manager while it is already stopped")
        } else {
            isStarted = false
            Timber.tag(TAG).d("Stopping update manager")
            disconnectSocketClient()
        }
    }

    private suspend fun connectSocketClient() {
        if (state == UpdatesState.CONNECTED) {
            Timber.tag(TAG).d("Trying to connect update manager while it is already connected")
            return
        }

        state = UpdatesState.INITIALIZING
        Timber.tag(TAG).d("Connecting: Waiting for network")

        try {
            withTimeout(DELAY_MS) { connectionStateProvider.awaitNetwork() }
            Timber.tag(TAG).d("Connecting: Network OK, initializing update handlers")
            updateHandlers.forEach { it.initialize() }
        } catch (e: Throwable) {
            state = UpdatesState.INITIALIZATION_FAILED
            return
        }

        state = UpdatesState.CONNECTING
        Timber.tag(TAG).d("Connecting: Update handlers are initialized, connecting to event stream")

        state = UpdatesState.CONNECTED
        try {
            client = SubscriptionWebSocketClient.getInstance(endpoint, this)
        } catch (e: Throwable) {
            state = UpdatesState.CONNECTING_FAILED
        }
    }

    private fun disconnectSocketClient() {
        client?.close()
        state = UpdatesState.DISCONNECTED
        Timber.tag(TAG).d("Disconnected from socket client")
    }

    private fun restartInternal() {
        if (!isStarted) {
            Timber.tag(TAG).d("Stopped")
        } else {
            Timber.tag(TAG).d("Reconnecting")
            launch {
                delay(DELAY_MS)
                client?.reconnect()
            }
        }
    }
}
