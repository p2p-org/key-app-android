package org.p2p.wallet.swap.jupiter.repository.model

import org.p2p.wallet.utils.LamportsAmount

data class SwapFees(
    val signatureFee: LamportsAmount,
    /**
     * the total amount needed for deposit of serum order account(s).
     */
    val openOrdersDeposits: List<LamportsAmount>,
    /**
     * the total amount needed for deposit of associative token account(s).
     */
    val ataDeposits: List<LamportsAmount>,
    /**
     * the total lamports needed for fees and deposits above
     */
    val totalFeeAndDeposits: LamportsAmount,
    val minimumSolForTransaction: LamportsAmount
)
