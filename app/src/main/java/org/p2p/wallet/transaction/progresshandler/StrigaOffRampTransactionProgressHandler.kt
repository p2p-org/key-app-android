package org.p2p.wallet.transaction.progresshandler

import org.p2p.core.glide.GlideManager
import org.p2p.wallet.R
import org.p2p.wallet.transaction.model.NewShowProgress
import org.p2p.wallet.transaction.model.progressstate.StrigaOffRampTransactionState
import org.p2p.wallet.transaction.model.progressstate.TransactionState

class StrigaOffRampTransactionProgressHandler(
    glideManager: GlideManager
) : TransactionProgressHandler(glideManager) {

    companion object {
        const val QUALIFIER = "StrigaOffRampTransactionProgressHandler"
    }

    override fun handleInitState(showProgressData: NewShowProgress) {
        super.handleInitState(showProgressData)
        binding.progressStateTransaction.setDescriptionText(R.string.striga_withdraw_transaction_details)
    }

    override fun handleState(state: TransactionState) {
        super.handleState(state)
        when (state) {
            is StrigaOffRampTransactionState.UsdcWithdrawSuccess -> {
                setSuccessState()
                // rewrite description text
                binding.progressStateTransaction.setDescriptionText(R.string.striga_withdraw_transaction_details)
            }
            is StrigaOffRampTransactionState.UsdcWithdrawError -> {
                setErrorState()
            }
            is StrigaOffRampTransactionState.EurWithdrawSuccess -> {
                setSuccessState()
            }
            else -> {
                // do nothing
            }
        }
    }
}
