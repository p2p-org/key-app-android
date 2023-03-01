package org.p2p.ethereumkit.api.core

import org.p2p.ethereumkit.api.jsonrpc.BlockNumberJsonRpc
import org.p2p.ethereumkit.api.jsonrpc.JsonRpc
import org.p2p.ethereumkit.core.EthereumKit
import org.p2p.ethereumkit.network.ConnectionManager
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.concurrent.schedule

class ApiRpcSyncer(
    private val rpcApiProvider: IRpcApiProvider,
    private val connectionManager: ConnectionManager,
    private val syncInterval: Long,
) : IRpcSyncer {
    private val disposables = CompositeDisposable()
    private var isStarted = false
    private var timer: Timer? = null

    init {
        connectionManager.listener = object : ConnectionManager.Listener {
            override fun onConnectionChange() {
                handleConnectionChange()
            }
        }
    }

    //region IRpcSyncer
    override var listener: IRpcSyncerListener? = null
    override val source = "API ${rpcApiProvider.source}"
    override var state: SyncerState = SyncerState.NotReady(EthereumKit.SyncError.NotStarted())
        private set(value) {
            if (value != field) {
                field = value
                listener?.didUpdateSyncerState(value)
            }
        }

    override fun start() {
        isStarted = true

        handleConnectionChange()
    }

    override fun stop() {
        isStarted = false

        state = SyncerState.NotReady(EthereumKit.SyncError.NotStarted())
        disposables.clear()
        stopTimer()
    }

    override fun <T> single(rpc: JsonRpc<T>): Single<T> =
        rpcApiProvider.single(rpc)
    //endregion

    private fun handleConnectionChange() {
        if (!isStarted) return

        if (connectionManager.isConnected) {
            state = SyncerState.Ready
            startTimer()
        } else {
            state = SyncerState.NotReady(EthereumKit.SyncError.NoNetworkConnection())
            stopTimer()
        }
    }

    private fun startTimer() {
        timer = Timer().apply {
            schedule(0, syncInterval * 1000) {
                onFireTimer()
            }
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun onFireTimer() {
        rpcApiProvider.single(BlockNumberJsonRpc())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ lastBlockNumber ->
                    listener?.didUpdateLastBlockHeight(lastBlockNumber)
                }, {
                    state = SyncerState.NotReady(it)
                }).let {
                    disposables.add(it)
                }
    }

}
