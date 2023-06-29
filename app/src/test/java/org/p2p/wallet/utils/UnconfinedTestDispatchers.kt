package org.p2p.wallet.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.p2p.ethereumkit.external.core.DefaultDispatchers
import org.p2p.core.dispatchers.CoroutineDispatchers

/**
 * These dispatchers executes everything immediately, which is very handy for testing complex things
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UnconfinedTestDispatchers : DefaultDispatchers(), CoroutineDispatchers {
    override val computation: CoroutineDispatcher
        get() = UnconfinedTestDispatcher()
    override val io: CoroutineDispatcher
        get() = UnconfinedTestDispatcher()
    override val ui: CoroutineDispatcher
        get() = UnconfinedTestDispatcher()

    init {
        Dispatchers.setMain(ui)
    }
}
