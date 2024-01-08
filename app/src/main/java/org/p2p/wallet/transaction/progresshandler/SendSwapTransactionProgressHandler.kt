package org.p2p.wallet.transaction.progresshandler

import timber.log.Timber
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.transaction.model.progressstate.SendSwapProgressState
import org.p2p.wallet.transaction.model.progressstate.TransactionState

class SendSwapTransactionProgressHandler(
    glideManager: GlideManager
) : TransactionProgressHandler(glideManager) {

    companion object {
        const val QUALIFIER = "SendSwapHandler"
    }

    override fun handleState(state: TransactionState) {
        super.handleState(state)
        when (state) {
            is SendSwapProgressState.Success -> setSuccessState()
            is SendSwapProgressState.Error -> setErrorState()
            else -> {
                Timber.e("Unsupported State for SendSwapTransactionProgressHandler: $state")
            }
        }
    }
}
