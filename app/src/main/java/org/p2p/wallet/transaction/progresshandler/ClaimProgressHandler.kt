package org.p2p.wallet.transaction.progresshandler

import timber.log.Timber
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.getString
import org.p2p.wallet.R
import org.p2p.wallet.transaction.model.progressstate.ClaimProgressState
import org.p2p.wallet.transaction.model.progressstate.TransactionState

class ClaimProgressHandler(
    glideManager: GlideManager
) : TransactionProgressHandler(glideManager) {

    companion object {
        const val QUALIFIER = "ClaimHandler"
    }

    override fun handleState(state: TransactionState) {
        super.handleState(state)
        when (state) {
            is ClaimProgressState.Success -> setSuccessState()
            is ClaimProgressState.Error -> setErrorState()
            else -> {
                Timber.e("Unsupported State for ClaimProgressHandler: $state")
            }
        }
    }

    private fun handleProgress() {
        with(binding) {
            val message = getString(R.string.bridge_claim_description_progress)
            textViewTitle.text = progressStateFormat.format(getString(R.string.transaction_progress_submitted))
            progressStateTransaction.setDescriptionText(message)
            buttonDone.setText(R.string.common_done)
        }
    }
}
