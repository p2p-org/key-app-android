package org.p2p.wallet.swap.model.orca

sealed class OrcaSwapResult {
    data class Finished(val transactionId: String, val destinationAddress: String) : OrcaSwapResult()
    object InvalidInfoOrPair : OrcaSwapResult()
    object InvalidPool : OrcaSwapResult()
}
