package org.p2p.wallet.feerelayer.interactor

import org.p2p.solanaj.core.Account
import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class FeeRelayerInstructionsInteractor(
    private val tokenKeyProvider: TokenKeyProvider,
    private val userLocalRepository: UserLocalRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor
) {

    /*
    * Prepare swap data from swap pools
    * */
    fun prepareSwapData(
        pools: OrcaPoolsPair,
        inputAmount: BigInteger?,
        minAmountOut: BigInteger?,
        slippage: Double,
        transitTokenMintPubkey: PublicKey? = null,
        newTransferAuthority: Boolean = true,
        userAuthorityAddress: PublicKey
    ): Pair<SwapData, Account?> {
        val owner = tokenKeyProvider.publicKey.toPublicKey()
        // preconditions
        if (pools.size == 0 || pools.size > 2) {
            throw IllegalStateException("Swap pools not found")
        }

        if (inputAmount == null && minAmountOut == null) {
            throw IllegalStateException("Invalid amount")
        }

        // create transferAuthority
        val transferAuthority = Account()

        // form topUp params
        if (pools.size == 1) {
            val pool = pools[0]

            val amountIn = inputAmount ?: pool.getInputAmount(minAmountOut!!, slippage)
            val minAmountOut = minAmountOut ?: pool.getMinimumAmountOut(inputAmount!!, slippage)

            if (amountIn == null || minAmountOut == null) {
                throw IllegalStateException("Invalid amount")
            }

            val directSwapData = pool.getSwapData(
                transferAuthorityPubkey = if (newTransferAuthority) transferAuthority.publicKey else owner,
                amountIn = amountIn,
                minAmountOut = minAmountOut
            )

            return directSwapData to if (newTransferAuthority) transferAuthority else null
        } else {
            val firstPool = pools[0]
            val secondPool = pools[1]

            if (transitTokenMintPubkey == null) {
                throw IllegalStateException("Transit token mint not found")
            }

            // if input amount is provided
            var firstPoolAmountIn = inputAmount
            var secondPoolAmountIn: BigInteger? = null
            var secondPoolAmountOut = minAmountOut

            if (inputAmount != null) {
                secondPoolAmountIn = firstPool.getMinimumAmountOut(inputAmount, slippage) ?: BigInteger.ZERO
                secondPoolAmountOut = secondPool.getMinimumAmountOut(secondPoolAmountIn!!, slippage)
            } else if (minAmountOut != null) {
                secondPoolAmountIn = secondPool.getInputAmount(minAmountOut, slippage) ?: BigInteger.ZERO
                firstPoolAmountIn = firstPool.getInputAmount(secondPoolAmountIn!!, slippage)
            }

            if (firstPoolAmountIn == null || secondPoolAmountIn == null || secondPoolAmountOut == null) {
                throw IllegalStateException("Invalid amount")
            }

            val transitiveSwapData = SwapData.SplTransitive(
                from = firstPool.getSwapData(
                    transferAuthorityPubkey = if (newTransferAuthority) transferAuthority.publicKey else owner,
                    amountIn = firstPoolAmountIn,
                    minAmountOut = secondPoolAmountIn
                ),
                to = secondPool.getSwapData(
                    transferAuthorityPubkey = if (newTransferAuthority) transferAuthority.publicKey else owner,
                    amountIn = secondPoolAmountIn,
                    minAmountOut = secondPoolAmountOut
                ),
                transitTokenMintPubkey = transitTokenMintPubkey.toBase58(),
                transitTokenAccountAddress = feeRelayerAccountInteractor.getTransitTokenAccountAddress(
                    owner = userAuthorityAddress,
                    mint = transitTokenMintPubkey
                )
            )

            return transitiveSwapData to if (newTransferAuthority) transferAuthority else null
        }
    }

    fun getTransitTokenMintPubkey(pools: OrcaPoolsPair): PublicKey? {
        var transitTokenMintPubkey: PublicKey? = null
        if (pools.size == 2) {
            val interTokenName = pools[0].tokenBName
            val pubkey = userLocalRepository.findTokenDataBySymbol(interTokenName)?.mintAddress
            transitTokenMintPubkey = pubkey?.let { PublicKey(pubkey) }
        }
        return transitTokenMintPubkey
    }

    private fun OrcaPool.getSwapData(
        transferAuthorityPubkey: PublicKey,
        amountIn: BigInteger,
        minAmountOut: BigInteger
    ): SwapData.Direct =
        SwapData.Direct(
            programId = swapProgramId.toBase58(),
            accountPubkey = account.toBase58(),
            authorityPubkey = authority.toBase58(),
            transferAuthorityPubkey = transferAuthorityPubkey.toBase58(),
            sourcePubkey = tokenAccountA.toBase58(),
            destinationPubkey = tokenAccountB.toBase58(),
            poolTokenMintPubkey = poolTokenMint.toBase58(),
            poolFeeAccountPubkey = feeAccount.toBase58(),
            amountIn = amountIn,
            minimumAmountOut = minAmountOut
        )
}