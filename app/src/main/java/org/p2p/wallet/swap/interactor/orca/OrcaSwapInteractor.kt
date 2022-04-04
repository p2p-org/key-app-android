package org.p2p.wallet.swap.interactor.orca

import org.p2p.solanaj.core.FeeAmount
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerSwapInteractor
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.OrcaSwapResult
import org.p2p.wallet.swap.model.orca.SwapFee
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.AmountInLamports
import org.p2p.wallet.utils.Constants.SOL_SYMBOL
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.scaleMedium
import org.p2p.wallet.utils.toLamportsValue
import org.p2p.wallet.utils.toPublicKey
import org.p2p.wallet.utils.toUsd
import java.math.BigDecimal
import java.math.BigInteger

class OrcaSwapInteractor(
    private val feeRelayerSwapInteractor: FeeRelayerSwapInteractor,
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val orcaRouteInteractor: OrcaRouteInteractor,
    private val orcaInfoInteractor: OrcaInfoInteractor,
    private val orcaPoolInteractor: OrcaPoolInteractor,
    private val userInteractor: UserInteractor,
    private val rpcAmountRepository: RpcAmountRepository,
    private val orcaNativeSwapInteractor: OrcaNativeSwapInteractor,
    private val environmentManager: EnvironmentManager,
    private val tokenKeyProvider: TokenKeyProvider
) {

    @JvmInline
    value class FeePayerUpdateResult(val isUpdated: Boolean)

    /*
    * If transaction will need to create a new account,
    * then the fee for account creation will be paid via this token
    * */
    private lateinit var feePayerToken: Token.Active

    /*
    * Initialize fee payer token
    * */
    suspend fun initialize(sol: Token.Active) {
        feePayerToken = sol
        orcaInfoInteractor.load()
        feeRelayerInteractor.load()
    }

    fun getFeePayerToken(): Token.Active = feePayerToken

    fun setFeePayerToken(newToken: Token.Active) {
        if (!this::feePayerToken.isInitialized) throw IllegalStateException("PayToken is not initialized")
        if (newToken.publicKey.equals(feePayerToken)) return

        feePayerToken = newToken
    }

    suspend fun getFreeTransactionsInfo(): FreeTransactionFeeLimit {
        return feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
    }

    suspend fun initialize() {
        feeRelayerInteractor.load()
    }

    // Execute swap
    suspend fun swap(
        fromToken: Token.Active,
        toToken: Token,
        bestPoolsPair: OrcaPoolsPair,
        amount: BigInteger,
        slippage: Slippage
    ): OrcaSwapResult {

        return if (isNativeSwap(fromToken.publicKey, feePayerToken.mintAddress)) {
            swapNative(
                poolsPair = bestPoolsPair,
                sourceAddress = fromToken.publicKey,
                destinationAddress = toToken.publicKey,
                amount = amount,
                slippage = slippage.doubleValue,
            )
        } else {
            swapByFeeRelayer(
                sourceAddress = fromToken.publicKey,
                sourceTokenMint = fromToken.mintAddress,
                destinationAddress = toToken.publicKey,
                destinationTokenMint = toToken.mintAddress,
                poolsPair = bestPoolsPair,
                amount = amount,
                slippage = slippage.doubleValue,
            )
        }
    }

    private suspend fun swapNative(
        poolsPair: OrcaPoolsPair,
        sourceAddress: String,
        destinationAddress: String?,
        amount: BigInteger,
        slippage: Double
    ): OrcaSwapResult {

        return orcaNativeSwapInteractor.swap(
            fromWalletPubkey = sourceAddress,
            toWalletPubkey = destinationAddress,
            bestPoolsPair = poolsPair,
            amount = amount,
            slippage = slippage
        )
    }

    private suspend fun swapByFeeRelayer(
        sourceAddress: String,
        sourceTokenMint: String,
        destinationAddress: String?,
        destinationTokenMint: String,
        poolsPair: OrcaPoolsPair,
        amount: BigInteger,
        slippage: Double
    ): OrcaSwapResult {
        val feeRelayerProgramId = FeeRelayerProgram.getProgramId(environmentManager.isMainnet())

        val payingFeeToken = TokenInfo(feePayerToken.publicKey, feePayerToken.mintAddress)
        val transaction = feeRelayerSwapInteractor.prepareSwapTransaction(
            feeRelayerProgramId = feeRelayerProgramId,
            sourceToken = TokenInfo(sourceAddress, sourceTokenMint),
            destinationTokenMint = destinationTokenMint,
            destinationAddress = destinationAddress,
            payingFeeToken = payingFeeToken,
            swapPools = poolsPair,
            inputAmount = amount,
            slippage = slippage,
        )

        val transactionId = feeRelayerInteractor.topUpAndRelayTransaction(transaction, payingFeeToken)
            .firstOrNull().orEmpty()

        // todo: find destination address
        return OrcaSwapResult.Finished(transactionId, destinationAddress.orEmpty())
    }

    suspend fun calculateFeeAndNeededTopUpAmountForSwapping(
        sourceToken: Token.Active,
        destination: Token,
    ): SwapFee {
        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()
        val freeTransactionLimits = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
        val fee: FeeAmount = feeRelayerSwapInteractor.calculateSwappingNetworkFees(
            sourceTokenMint = sourceToken.mintAddress,
            destinationTokenMint = destination.mintAddress,
            destinationAddress = destination.publicKey
        )

        val accountCreationToken = if (destination is Token.Other) destination.tokenSymbol else SOL_SYMBOL
        val accountCreationFee = fee.total.fromLamports(feePayerToken.decimals).scaleMedium()
        val accountCreationFeeUsd = accountCreationFee.toUsd(feePayerToken.usdRate)

        val transactionNetworkFee = BigInteger.valueOf(2) * relayInfo.lamportsPerSignature
        val isFreeTransactionAvailable = freeTransactionLimits.isFreeTransactionFeeAvailable(transactionNetworkFee)

        val transactionFee = transactionNetworkFee.fromLamports(feePayerToken.decimals).scaleMedium()
        val transactionFeeUsd = transactionFee.toUsd(feePayerToken.usdRate)

        val totalFee = AmountInLamports(fee.total + transactionNetworkFee)

        return SwapFee(
            isFreeTransactionAvailable = isFreeTransactionAvailable,
            accountCreationToken = accountCreationToken,
            accountCreationFee = accountCreationFee,
            accountCreationFeeUsd = accountCreationFeeUsd,
            transactionFee = transactionFee,
            transactionFeeUsd = transactionFeeUsd,
            feePayerToken = feePayerToken.tokenSymbol,
            totalInLamports = totalFee
        )
    }

    /**
     * Check that the fee payer token has enough funds to pay for the fee.
     * Try to check that the fee can be paid with the user SOL token, then check it with a non-sol source token.
     */
    suspend fun updateFeePayerToken(
        totalSwapFee: AmountInLamports,
        sourceToken: Token.Active,
    ): FeePayerUpdateResult {
        val newFeePayer = if (sourceToken.isSOL) {
            // SOL-token fees can be paid with SOL token only
            sourceToken
        } else {
            // non-SOL token fees can be paid with SOL token or non-SOL token
            if (canUserPayFeeWithSol(totalSwapFee)) {
                userInteractor.getUserTokens().find(Token.Active::isSOL)
            } else {
                // use non-sol token
                sourceToken
            }
        }
        return if (newFeePayer == null || newFeePayer.mintAddress == feePayerToken.mintAddress) {
            FeePayerUpdateResult(isUpdated = false)
        } else {
            setFeePayerToken(newFeePayer)
            FeePayerUpdateResult(isUpdated = true)
        }
    }

    /**
     * if user doesn't have enough non-sol source tokens to pay fee
     * check if user has SOL tokens to pay instead
     */
    private suspend fun canUserPayFeeWithSol(totalSwapFee: AmountInLamports): Boolean {
        val userSolToken = userInteractor.getUserTokens().find(Token.Active::isSOL) ?: return false
        val userSolTokenAmount = userSolToken.total.toLamportsValue(userSolToken.decimals)

        return userSolTokenAmount >= totalSwapFee
    }

    //
    // - Returns:
    /**
     * Get fees from current context
     * @return transactions fees (fees for signatures),
     * @return liquidity provider fees (fees in intermediary token?, fees in destination token)
     */
    suspend fun getFees(
        myWalletsMints: List<String>,
        fromWalletPubkey: String,
        toWalletPubkey: String?,
        feeRelayerFeePayerPubkey: String?,
        bestPoolsPair: OrcaPoolsPair?,
        inputAmount: BigDecimal?,
        slippage: Double
    ): Pair<BigInteger, List<BigInteger>> {
        val owner = tokenKeyProvider.publicKey.toPublicKey()

        val lamportsPerSignature = rpcAmountRepository.getLamportsPerSignature()
        val minRentExempt = rpcAmountRepository.getMinBalanceForRentExemption()

        var transactionFees: BigInteger = BigInteger.ZERO

        val numberOfPools = BigInteger.valueOf(bestPoolsPair?.size?.toLong() ?: 0L)

        var numberOfTransactions = BigInteger.ONE

        if (numberOfPools == BigInteger.valueOf(2L)) {
            val myTokens = myWalletsMints.mapNotNull { orcaPoolInteractor.getTokenFromMint(it) }.map { it.first }
            val intermediaryTokenName = bestPoolsPair!![0].tokenBName

            if (!myTokens.contains(intermediaryTokenName) || toWalletPubkey == null) {
                numberOfTransactions += BigInteger.ONE
            }

            if (intermediaryTokenName == SOL_SYMBOL) {
                transactionFees += lamportsPerSignature
                transactionFees += minRentExempt
            }
        }

        // owner's signatures
        transactionFees += lamportsPerSignature * numberOfTransactions

        if (feeRelayerFeePayerPubkey == null) {
            // userAuthority's signatures
            transactionFees += lamportsPerSignature * numberOfPools
        } else {
            throw IllegalStateException("feeRelayer is being implemented")
        }

        // when swap from/to native SOL, a fee for creating it is needed
        if (fromWalletPubkey == owner.toBase58() || bestPoolsPair!!.last().tokenBName == SOL_SYMBOL) {
            transactionFees += lamportsPerSignature
            transactionFees += minRentExempt
        }

        if (toWalletPubkey.isNullOrEmpty()) {
            transactionFees += lamportsPerSignature
            transactionFees += minRentExempt
        }

        val liquidityProviderFees = if (inputAmount != null) {
            orcaRouteInteractor.calculateLiquidityProviderFees(
                poolsPair = bestPoolsPair!!,
                inputAmount = inputAmount,
                slippage = slippage
            )
        } else emptyList()

        return transactionFees to liquidityProviderFees
    }

    suspend fun getFeesInPayingToken(feeInSOL: AmountInLamports): AmountInLamports {
        if (feePayerToken.isSOL) return feeInSOL

        return feeRelayerInteractor.calculateFeeInPayingToken(
            feeInSOL = FeeAmount(accountBalances = feeInSOL.amount),
            payingFeeTokenMint = feePayerToken.mintAddress
        ).total.let { AmountInLamports(it) }
    }

    suspend fun feePayerHasEnoughBalance(feeInSOL: AmountInLamports): Boolean {
        val feePayerLamports = feePayerToken.total.toLamportsValue(feePayerToken.decimals)
        val feeInPayingToken = getFeesInPayingToken(feeInSOL)
        return feePayerLamports >= feeInPayingToken
    }

    private fun isNativeSwap(
        sourceAddress: String,
        payingTokenMint: String?
    ): Boolean {
        val account = tokenKeyProvider.publicKey
        return sourceAddress == account || payingTokenMint == WRAPPED_SOL_MINT
    }
}
