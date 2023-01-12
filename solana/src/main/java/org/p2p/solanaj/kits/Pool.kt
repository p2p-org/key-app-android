package org.p2p.solanaj.kits

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.PublicKey.Companion.findProgramAddress
import org.p2p.solanaj.model.types.ProgramAccount
import org.p2p.solanaj.programs.TokenSwapProgram.TokenSwapData
import java.math.BigInteger

object Pool {

    class PoolInfo(
        val address: PublicKey,
        val swapProgramId: PublicKey,
        val swapData: TokenSwapData
    ) {
        val authority: PublicKey
            get() {
                return try {
                    findProgramAddress(
                        listOf(address.asByteArray()),
                        swapProgramId
                    )
                        .address
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            }
        val tokenProgramId: PublicKey
            get() = swapData.tokenProgramId
        val tokenAccountA: PublicKey
            get() = swapData.tokenAccountA
        val tokenAccountB: PublicKey
            get() = swapData.tokenAccountB
        val tokenPool: PublicKey
            get() = swapData.tokenPool
        val mintA: PublicKey
            get() = swapData.mintA
        val mintB: PublicKey
            get() = swapData.mintB
        val feeAccount: PublicKey
            get() = swapData.feeAccount
        val tradeFeeNumerator: BigInteger
            get() = swapData.tradeFeeNumerator
        val tradeFeeDenominator: BigInteger
            get() = swapData.tradeFeeDenominator

        companion object {
            fun fromProgramAccount(programAccount: ProgramAccount): PoolInfo {
                return PoolInfo(
                    PublicKey(programAccount.pubkey),
                    PublicKey(programAccount.account.owner),
                    TokenSwapData.decode(programAccount.account.getDecodedData())
                )
            }
        }
    }
}
