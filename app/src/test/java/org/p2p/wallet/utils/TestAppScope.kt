package org.p2p.wallet.utils

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import org.p2p.core.common.di.AppScope

class TestAppScope(dispatcher: CoroutineDispatcher) : AppScope() {
    override val coroutineContext: CoroutineContext = dispatcher
}
