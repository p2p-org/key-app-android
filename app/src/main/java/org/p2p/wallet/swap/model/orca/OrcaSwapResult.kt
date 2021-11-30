package org.p2p.wallet.swap.model.orca

sealed class OrcaSwapResult {
    data class Executing(val transactionId: String) : OrcaSwapResult()
    object InvalidInfoOrPair : OrcaSwapResult()
    object InvalidPool : OrcaSwapResult()
}