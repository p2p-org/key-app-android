package org.p2p.wallet.feerelayer.interactor

import org.p2p.solanaj.core.PublicKey
import org.p2p.wallet.feerelayer.model.SwapData
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.swap.model.orca.OrcaPool
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.toPublicKey
import java.math.BigInteger

class FeeRelayerInstructionsInteractor(
    private val tokenKeyProvider: TokenKeyProvider,
    private val userLocalRepository: UserLocalRepository,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val addressInteractor: TransactionAddressInteractor
) {

    /*
    * Prepare swap data from swap pools
    * */
    suspend fun prepareSwapData(
        pools: OrcaPoolsPair,
        inputAmount: BigInteger?,
        minAmountOut: BigInteger?,
        slippage: Double,
        transitTokenMintPubkey: PublicKey?,
        userAuthorityAddress: PublicKey
    ): SwapData {
        val owner = tokenKeyProvider.publicKey.toPublicKey()
        // preconditions
        if (pools.size == 0 || pools.size > 2) {
            throw IllegalStateException("Swap pools not found")
        }

        if (inputAmount == null && minAmountOut == null) {
            throw IllegalStateException("Invalid amount")
        }

        // form topUp params
        if (pools.size == 1) {
            val pool = pools[0]

            val amountIn = inputAmount ?: pool.getInputAmount(minAmountOut!!, slippage)
            val minAmountOut = minAmountOut ?: pool.getMinimumAmountOut(inputAmount!!, slippage)

            if (amountIn == null || minAmountOut == null) {
                throw IllegalStateException("Invalid amount")
            }

            return pool.getSwapData(
                transferAuthorityPubkey = owner,
                amountIn = amountIn,
                minAmountOut = minAmountOut
            )
        } else {
            val firstPool = pools[0]
            val secondPool = pools[1]

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

            if (transitTokenMintPubkey == null) throw IllegalStateException("Transit token mint is null")

            val transitTokenAccountAddressData = feeRelayerAccountInteractor.getTransitTokenAccountAddress(
                owner = userAuthorityAddress,
                mint = transitTokenMintPubkey
            )

            val transitTokenAccountAddressAccount = addressInteractor.findSplTokenAddressData(
                destinationAddress = transitTokenAccountAddressData,
                mintAddress = transitTokenMintPubkey.toBase58()
            )

            return SwapData.SplTransitive(
                from = firstPool.getSwapData(
                    transferAuthorityPubkey = owner,
                    amountIn = firstPoolAmountIn,
                    minAmountOut = secondPoolAmountIn
                ),
                to = secondPool.getSwapData(
                    transferAuthorityPubkey = owner,
                    amountIn = secondPoolAmountIn,
                    minAmountOut = secondPoolAmountOut
                ),
                transitTokenMintPubkey = transitTokenMintPubkey.toBase58(),
                transitTokenAccountAddress = transitTokenAccountAddressData,
                needsCreateTransitTokenAccount = transitTokenAccountAddressAccount.shouldCreateAccount
            )
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

    fun getTransitToken(
        pools: OrcaPoolsPair
    ): TokenInfo? {
        val owner = tokenKeyProvider.publicKey
        val transitTokenMintPubkey = getTransitTokenMintPubkey(pools)

        var transitTokenAccountAddress: PublicKey? = null
        if (transitTokenMintPubkey != null) {
            transitTokenAccountAddress = feeRelayerAccountInteractor.getTransitTokenAccountAddress(
                owner = owner.toPublicKey(),
                mint = transitTokenMintPubkey
            )
        }

        if (transitTokenMintPubkey != null && transitTokenAccountAddress != null) {
            return TokenInfo(
                address = transitTokenAccountAddress.toBase58(),
                mint = transitTokenMintPubkey.toBase58()
            )
        }
        return null
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
