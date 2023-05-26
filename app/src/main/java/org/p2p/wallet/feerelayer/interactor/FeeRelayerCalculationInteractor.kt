package org.p2p.wallet.feerelayer.interactor

import timber.log.Timber
import java.math.BigInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.isZero
import org.p2p.solanaj.core.FeeAmount
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeePoolsState
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.rpc.interactor.TransactionAddressInteractor
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.utils.toPublicKey

private const val TAG = "FeeRelayerCalculationInteractor"

class FeeRelayerCalculationInteractor(
    private val amountRepository: RpcAmountRepository,
    private val addressInteractor: TransactionAddressInteractor,
    private val feeRelayerTopUpInteractor: FeeRelayerTopUpInteractor,
    private val feeRelayerInteractor: FeeRelayerInteractor,
    private val dispatchers: CoroutineDispatchers
) {

    // Fees calculator
    suspend fun calculateFeesForFeeRelayer(
        feePayerToken: Token.Active,
        token: Token.Active,
        recipient: String,
        useCache: Boolean = true
    ): FeeCalculationState {
        try {
            val lamportsPerSignature: BigInteger = amountRepository.getLamportsPerSignature(null)
            val minRentExemption: BigInteger =
                amountRepository.getMinBalanceForRentExemption(TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH)

            var transactionFee = BigInteger.ZERO

            // owner's signature
            transactionFee += lamportsPerSignature

            // feePayer's signature
            if (!feePayerToken.isSOL) {
                Timber.tag(TAG).i("Fee payer is not sol, adding $lamportsPerSignature for fee")
                transactionFee += lamportsPerSignature
            }

            val shouldCreateAccount =
                token.mintAddress != Constants.WRAPPED_SOL_MINT && addressInteractor.findSplTokenAddressData(
                    mintAddress = token.mintAddress,
                    destinationAddress = recipient.toPublicKey(),
                    useCache = useCache
                ).shouldCreateAccount

            Timber.tag(TAG).i("Should create account = $shouldCreateAccount")

            val expectedFee = FeeAmount(
                transaction = transactionFee,
                accountBalances = if (shouldCreateAccount) minRentExemption else BigInteger.ZERO
            )

            val fees = feeRelayerTopUpInteractor.calculateNeededTopUpAmount(expectedFee)

            if (fees.total.isZero()) {
                Timber.tag(TAG).i("Total fees are zero!")
                return FeeCalculationState.NoFees
            }

            val poolsStateFee = getFeesInPayingToken(
                feePayerToken = feePayerToken,
                transactionFeeInSOL = fees.transaction,
                accountCreationFeeInSOL = fees.accountBalances
            )

            return when (poolsStateFee) {
                is FeePoolsState.Calculated -> {
                    Timber.tag(TAG).i("FeePoolsState is calculated")
                    FeeCalculationState.Success(FeeRelayerFee(fees, poolsStateFee.feeInSpl, expectedFee))
                }

                is FeePoolsState.Failed -> {
                    Timber.tag(TAG).i("FeePoolsState is failed")
                    FeeCalculationState.PoolsNotFound(FeeRelayerFee(fees, poolsStateFee.feeInSOL, expectedFee))
                }
            }
        } catch (e: CancellationException) {
            Timber.tag(TAG).i("Fee calculation cancelled")
            return FeeCalculationState.Cancelled
        } catch (e: Throwable) {
            Timber.tag(TAG).i(e, "Failed to calculateFeesForFeeRelayer")
            return FeeCalculationState.Error(e)
        }
    }

    /*
   * The request is too complex
   * Wrapped each request into deferred
   * TODO: Create a function to find fees by multiple tokens
   * */
    suspend fun findAlternativeFeePayerTokens(
        userTokens: List<Token.Active>,
        feePayerToExclude: Token.Active,
        transactionFeeInSOL: BigInteger,
        accountCreationFeeInSOL: BigInteger
    ): List<Token.Active> = withContext(dispatchers.io) {
        val tokenToExclude = feePayerToExclude.tokenSymbol
        val fees = userTokens
            .map { token ->
                // converting SOL fee in token lamports to verify the balance coverage
                async { getFeesInPayingTokenNullable(token, transactionFeeInSOL, accountCreationFeeInSOL) }
            }
            .awaitAll()
            .filterNotNull()
            .toMap()

        Timber.tag(TAG).i(
            "Filtering user tokens for alternative fee payers: ${userTokens.map(Token.Active::mintAddress)}"
        )
        userTokens.filter { token ->
            if (token.tokenSymbol == tokenToExclude) {
                Timber.tag(TAG)
                    .i("Excluding ${token.mintAddress} ${token.tokenSymbol}")
                return@filter false
            }

            val totalInSol = transactionFeeInSOL + accountCreationFeeInSOL
            if (token.isSOL) {
                Timber.tag(TAG)
                    .i("Checking SOL as fee payer = ${token.totalInLamports >= totalInSol}")
                return@filter token.totalInLamports >= totalInSol
            }

            // assuming that all other tokens are SPL
            val feesInSpl = fees[token.tokenSymbol] ?: return@filter run {
                Timber.tag(TAG)
                    .i("Fee in SPL not found for ${token.tokenSymbol} in ${fees.keys}")
                false
            }
            token.totalInLamports >= feesInSpl.total
        }.also {
            Timber.tag(TAG)
                .i("Found alternative feepayer tokens: ${it.map(Token.Active::mintAddress)}")
        }
    }

    private suspend fun getFeesInPayingToken(
        feePayerToken: Token.Active,
        transactionFeeInSOL: BigInteger,
        accountCreationFeeInSOL: BigInteger
    ): FeePoolsState {
        Timber.tag(TAG)
            .i("Fetching fees in paying token: ${feePayerToken.mintAddress}")
        if (feePayerToken.isSOL) {
            val fee = FeeAmount(
                transaction = transactionFeeInSOL,
                accountBalances = accountCreationFeeInSOL
            )
            return FeePoolsState.Calculated(fee)
        }

        return feeRelayerInteractor.calculateFeeInPayingToken(
            feeInSOL = FeeAmount(transaction = transactionFeeInSOL, accountBalances = accountCreationFeeInSOL),
            payingFeeTokenMint = feePayerToken.mintAddress
        )
    }

    private suspend fun getFeesInPayingTokenNullable(
        token: Token.Active,
        transactionFeeInSOL: BigInteger,
        accountCreationFeeInSOL: BigInteger
    ): Pair<String, FeeAmount>? = try {
        Timber.tag(TAG).i("getFeesInPayingTokenNullable for ${token.mintAddress}")
        val feeInSpl = getFeesInPayingToken(
            feePayerToken = token,
            transactionFeeInSOL = transactionFeeInSOL,
            accountCreationFeeInSOL = accountCreationFeeInSOL
        )

        if (feeInSpl is FeePoolsState.Calculated) {
            Timber.tag(TAG).i("Fees found for ${token.mintAddress}")
            token.tokenSymbol to feeInSpl.feeInSpl
        } else {
            Timber.tag(TAG).i("Fees are null found for ${token.mintAddress}")
            null
        }
    } catch (e: IllegalStateException) {
        Timber.tag(TAG).i(e, "No fees found for token ${token.mintAddress}")
        // ignoring tokens without fees
        null
    }
}
