package org.p2p.wallet.newsend.smartselection

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token

class SmartSelectionCoordinator(
    dispatchers: CoroutineDispatchers
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.singleThreadDispatcher

    private lateinit var feePayer: Token.Active

    fun updateFeePayer(newFeePayer: Token.Active) {
        this.feePayer = newFeePayer
    }

    fun onNewTrigger(newTrigger: SmartSelectionTrigger) {
    }

    fun requireFeePayer() : Token.Active = feePayer

    fun release() {

    }
}
