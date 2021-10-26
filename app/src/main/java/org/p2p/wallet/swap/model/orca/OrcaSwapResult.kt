package org.p2p.wallet.swap.model.orca

import androidx.annotation.StringRes

sealed class OrcaSwapResult {
    data class Success(val transactionId: String) : OrcaSwapResult()
    data class Error(@StringRes val messageRes: Int) : OrcaSwapResult()
    object InvalidInfoOrPair : OrcaSwapResult()
    object InvalidPool : OrcaSwapResult()
}