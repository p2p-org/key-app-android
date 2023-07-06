package org.p2p.wallet.jupiter.statemanager

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.p2p.core.dispatchers.CoroutineDispatchers

class SwapCoroutineScope(
    dispatchers: CoroutineDispatchers
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.io
}
