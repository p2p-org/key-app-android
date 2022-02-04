package org.p2p.wallet.feerelayer.model

import java.math.BigInteger

sealed class SwapData {

    data class Spl(
        val programId: String,
        val accountPubkey: String,
        val authorityPubkey: String,
        val transferAuthorityPubkey: String,
        val sourcePubkey: String,
        val destinationPubkey: String,
        val poolTokenMintPubkey: String,
        val poolFeeAccountPubkey: String,
        val amountIn: BigInteger,
        val minimumAmountOut: BigInteger
    ) : SwapData()

    data class SplTransitive(
        val from: Spl,
        val to: Spl,
        val transitTokenMintPubkey: String
    ) : SwapData()
}