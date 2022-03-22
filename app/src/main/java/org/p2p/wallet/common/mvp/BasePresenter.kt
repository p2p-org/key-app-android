package org.p2p.wallet.common.mvp

import androidx.annotation.CallSuper
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class BasePresenter<V : MvpView> : MvpPresenter<V>, CoroutineScope {

    override val coroutineContext: CoroutineContext =
        SupervisorJob() +
            Dispatchers.Main.immediate +
            CoroutineExceptionHandler(::handleCoroutineException)

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
        Timber.tag(this::class.java.simpleName).e(throwable, coroutineContext.toString())
    }
}
