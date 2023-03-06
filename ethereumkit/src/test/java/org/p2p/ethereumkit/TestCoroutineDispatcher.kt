package org.p2p.ethereumkit

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.p2p.ethereumkit.external.core.DefaultDispatchers

@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineDispatcher : DefaultDispatchers() {
    override val computation: CoroutineDispatcher
        get() = StandardTestDispatcher()
    override val io: CoroutineDispatcher
        get() = StandardTestDispatcher()
    override val ui: CoroutineDispatcher
        get() = StandardTestDispatcher()
}
