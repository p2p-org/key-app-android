package org.p2p.wallet.swap.interactor.orca

import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.core.OperationType
import org.p2p.wallet.feerelayer.interactor.FeeRelayerAccountInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerSwapInteractor
import org.p2p.wallet.feerelayer.interactor.FeeRelayerTopUpInteractor
import org.p2p.wallet.feerelayer.model.FeeRelayerStatistics
import org.p2p.wallet.feerelayer.model.FreeTransactionFeeLimit
import org.p2p.wallet.feerelayer.model.TokenInfo
import org.p2p.wallet.feerelayer.program.FeeRelayerProgram
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.infrastructure.network.environment.EnvironmentManager
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.swap.model.FeeRelayerSwapFee
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.swap.model.orca.OrcaPoolsPair
import org.p2p.wallet.swap.model.orca.OrcaSwapResult
import org.p2p.wallet.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.utils.isZero
import java.math.BigInteger

class OrcaSwapInteractor(
    private val feeRelayerSwapInteractor: FeeRelayerSwapInteractor,
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val feeRelayerAccountInteractor: FeeRelayerAccountInteractor,
    private val feeRelayerTopUpInteractor: FeeRelayerTopUpInteractor,
    private val orcaRouteInteractor: OrcaRouteInteractor,
    private val orcaInfoInteractor: OrcaInfoInteractor,
    private val orcaPoolInteractor: OrcaPoolInteractor,
    private val rpcAmountRepository: RpcAmountRepository,
    private val orcaNativeSwapInteractor: OrcaNativeSwapInteractor,
    private val environmentManager: EnvironmentManager,
    private val tokenKeyProvider: TokenKeyProvider
) {

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

    // Execute swap
    suspend fun swap(
        fromToken: Token.Active,
        toToken: Token,
        bestPoolsPair: OrcaPoolsPair,
        amount: BigInteger,
        slippage: Slippage
    ): OrcaSwapResult {

        return if (shouldUseNativeSwap(feePayerToken.mintAddress)) {
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
        val (transaction, additionalPaybackFee) = feeRelayerSwapInteractor.prepareSwapTransaction(
            feeRelayerProgramId = feeRelayerProgramId,
            sourceToken = TokenInfo(sourceAddress, sourceTokenMint),
            destinationTokenMint = destinationTokenMint,
            destinationAddress = destinationAddress,
            payingFeeToken = payingFeeToken,
            swapPools = poolsPair,
            inputAmount = amount,
            slippage = slippage,
        )

        val statistics = FeeRelayerStatistics(
            operationType = OperationType.SWAP,
            currency = sourceTokenMint
        )

        val transactionId = feeRelayerInteractor.topUpAndRelayTransaction(
            preparedTransaction = transaction,
            payingFeeToken = payingFeeToken,
            additionalPaybackFee = additionalPaybackFee,
            statistics = statistics
        )
            .firstOrNull().orEmpty()

        // todo: find destination address
        return OrcaSwapResult.Finished(transactionId, destinationAddress.orEmpty())
    }

    // Fees calculator
    suspend fun calculateFeesForFeeRelayer(
        feePayerToken: Token.Active,
        sourceToken: Token.Active,
        destination: Token
    ): FeeRelayerSwapFee? {
        val expectedFee = feeRelayerSwapInteractor.calculateSwappingNetworkFees(
            sourceTokenMint = sourceToken.mintAddress,
            destinationTokenMint = destination.mintAddress,
            destinationAddress = destination.publicKey
        )

        val fees = feeRelayerTopUpInteractor.calculateNeededTopUpAmount(expectedFee)

        if (fees.total.isZero()) return null

        val relayInfo = feeRelayerAccountInteractor.getRelayInfo()
        val freeTransactionLimits = feeRelayerAccountInteractor.getFreeTransactionFeeLimit()
        val transactionNetworkFee = BigInteger.valueOf(2) * relayInfo.lamportsPerSignature
        val isFreeTransactionAvailable = freeTransactionLimits.isFreeTransactionFeeAvailable(transactionNetworkFee)

        return FeeRelayerSwapFee(
            feeInSol = fees.total,
            feeInPayingToken = getFeesInPayingToken(feePayerToken, fees.total),
            isFreeTransactionAvailable = isFreeTransactionAvailable
        )
    }

    suspend fun getFeesInPayingToken(
        feePayerToken: Token.Active,
        feeInSOL: BigInteger
    ): BigInteger {
        if (feePayerToken.isSOL) return feeInSOL

        return feeRelayerInteractor.calculateFeeInPayingToken(
            feeInSOL = FeeAmount(accountBalances = feeInSOL),
            payingFeeTokenMint = feePayerToken.mintAddress
        ).total
    }

    /*
    * When free transaction is not available and user is paying with sol,
    * let him do this the normal way (don't use fee relayer)
    * */
    private suspend fun shouldUseNativeSwap(payingTokenMint: String): Boolean {
        val noFreeTransactionsLeft = feeRelayerAccountInteractor.getFreeTransactionFeeLimit().remaining == 0
        val isSol = payingTokenMint == WRAPPED_SOL_MINT
        return noFreeTransactionsLeft && isSol
    }
}
