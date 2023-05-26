package org.p2p.wallet.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.p2p.ethereumkit.external.core.DefaultDispatchers
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers

@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineDispatcher : DefaultDispatchers(), CoroutineDispatchers {
    override val computation: CoroutineDispatcher
        get() = StandardTestDispatcher()
    override val io: CoroutineDispatcher
        get() = StandardTestDispatcher()
    override val ui: CoroutineDispatcher
        get() = StandardTestDispatcher()

    init {
        Dispatchers.setMain(ui)
    }
}
