package org.p2p.wallet.infrastructure.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal interface CoroutineDispatchers {
    val io: CoroutineDispatcher
    val computation: CoroutineDispatcher
    val ui: CoroutineDispatcher
}

class DefaultDispatchers : CoroutineDispatchers {
    override val io: CoroutineDispatcher
        get() = Dispatchers.IO
    override val computation: CoroutineDispatcher
        get() = Dispatchers.Default
    override val ui: CoroutineDispatcher
        get() = Dispatchers.Main.immediate
}
