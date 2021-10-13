package com.p2p.wallet.swap.orca.model

import org.p2p.solanaj.core.PublicKey
import java.math.BigInteger

data class OrcaPool(
    val address: PublicKey,
    val tokenPool: PublicKey,
    val feeAccount: PublicKey,
    val sourceMint: PublicKey,
    val destinationMint: PublicKey,
    val tokenAccountA: PublicKey,
    val tokenAccountB: PublicKey,
    val swapProgramId: PublicKey,
    val tradeFeeDenominator: BigInteger,
    val tradeFeeNumerator: BigInteger
) {

    fun swapData(): OrcaPool =
        this.copy(
            sourceMint = destinationMint,
            destinationMint = sourceMint,
            tokenAccountA = tokenAccountB,
            tokenAccountB = tokenAccountA,
            swapProgramId = swapProgramId
        )

    val authority: PublicKey
        get() {
            return try {
                PublicKey.findProgramAddress(
                    listOf(address.toByteArray()),
                    swapProgramId
                )
                    .address
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
}