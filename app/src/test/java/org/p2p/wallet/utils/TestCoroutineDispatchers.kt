package org.p2p.wallet.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.p2p.ethereumkit.external.core.DefaultDispatchers
import org.p2p.core.dispatchers.CoroutineDispatchers

@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineDispatchers(private val dispatcher: CoroutineDispatcher = StandardTestDispatcher()) :
    DefaultDispatchers(),
    CoroutineDispatchers {
    override val computation: CoroutineDispatcher
        get() = dispatcher
    override val io: CoroutineDispatcher
        get() = dispatcher
    override val ui: CoroutineDispatcher
        get() = dispatcher

    init {
        Dispatchers.setMain(dispatcher)
    }
}
