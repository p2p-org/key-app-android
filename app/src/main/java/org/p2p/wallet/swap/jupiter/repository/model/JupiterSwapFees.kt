package org.p2p.wallet.swap.jupiter.repository.model

import java.math.BigInteger

data class JupiterSwapFees(
    val signatureFee: BigInteger,
    /**
     * the total amount needed for deposit of serum order account(s).
     */
    val openOrdersDeposits: List<BigInteger>,
    /**
     * the total amount needed for deposit of associative token account(s).
     */
    val ataDeposits: List<BigInteger>,
    /**
     * the total lamports needed for fees and deposits above
     */
    val totalFeeAndDepositsInTokenB: BigInteger,
    val minimumSolForTransaction: BigInteger
)
