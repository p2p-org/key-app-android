package org.p2p.wallet.common.mvp

import androidx.annotation.CallSuper
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.p2p.core.network.ConnectionManager

abstract class BasePresenter<V : MvpView>(
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
) : MvpPresenter<V>, CoroutineScope {

    override val coroutineContext: CoroutineContext =
        SupervisorJob() + mainDispatcher + CoroutineExceptionHandler(::handleCoroutineException)

    protected var view: V? = null
        private set

    @CallSuper
    override fun attach(view: V) {
        this.view = view
    }

    @CallSuper
    override fun detach() {
        coroutineContext.cancelChildren()
        view = null
    }

    private fun handleCoroutineException(coroutineContext: CoroutineContext, throwable: Throwable) {
        if (throwable is CancellationException) {
            Timber.i(throwable)
        } else {
            Timber.tag(this::class.java.simpleName).e(throwable, coroutineContext.toString())
        }
    }

    protected fun launchSupervisor(block: suspend CoroutineScope.() -> Unit): Job {
        return launch {
            supervisorScope { block() }
        }
    }

    protected fun launchInternetAware(
        connectionManager: ConnectionManager,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        var job: Job? = null

        return launch {
            connectionManager.connectionStatus.collect { hasConnection ->
                if (hasConnection) {
                    job = launch { block() }
                } else {
                    job?.cancel()
                }
            }
        }
    }
}
