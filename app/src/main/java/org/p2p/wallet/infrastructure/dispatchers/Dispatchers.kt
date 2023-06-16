package org.p2p.wallet.infrastructure.dispatchers

import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher

interface CoroutineDispatchers {
    val io: CoroutineDispatcher
    val computation: CoroutineDispatcher
    val ui: CoroutineDispatcher

    val singleThreadDispatcher: CoroutineDispatcher
        get() = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}

class DefaultDispatchers : CoroutineDispatchers {
    override val io: CoroutineDispatcher
        get() = Dispatchers.IO
    override val computation: CoroutineDispatcher
        get() = Dispatchers.Default
    override val ui: CoroutineDispatcher
        get() = Dispatchers.Main.immediate
}
