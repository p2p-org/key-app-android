package org.p2p.wallet.transaction.progresshandler

import timber.log.Timber
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.transaction.model.progressstate.BridgeSendProgressState
import org.p2p.wallet.transaction.model.progressstate.TransactionState

class BridgeSendProgressHandler(
    glideManager: GlideManager
) : TransactionProgressHandler(glideManager) {

    companion object {
        const val QUALIFIER = "BridgeSendHandler"
    }

    override fun handleState(state: TransactionState) {
        super.handleState(state)
        when (state) {
            is BridgeSendProgressState.Success -> setSuccessState()
            is BridgeSendProgressState.Error -> setErrorState()
            else -> {
                Timber.d("Unsupported State: $state")
            }
        }
    }
}
