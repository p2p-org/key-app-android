package org.p2p.wallet.send.interactor.usecase

import timber.log.Timber
import java.math.BigInteger
import kotlin.coroutines.cancellation.CancellationException
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.core.utils.isZeroOrLess
import org.p2p.solanaj.core.FeeAmount
import org.p2p.token.service.converter.TokenServiceAmountsConverter
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeeRelayerFee
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.send.repository.SendServiceRepository
import org.p2p.wallet.send.repository.SendServiceTransactionError

class CalculateSendFeesUseCase(
    private val sendServiceRepository: SendServiceRepository,
    private val tokenKeyProvider: TokenKeyProvider,
    private val amountsConverter: TokenServiceAmountsConverter,
) {

    suspend fun execute(
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        sourceTokenAmount: BigInteger,
        searchResult: SearchResult,
        @Suppress("UNUSED_PARAMETER") useCache: Boolean = true
    ): FeeCalculationState {
        try {
            val rentExemptionInSol = sendServiceRepository.getTokenRentExemption(
                tokenMint = Constants.WRAPPED_SOL_MINT.toBase58Instance()
            )
            val fees = try {
                sendServiceRepository.estimateFees(
                    userWallet = tokenKeyProvider.publicKeyBase58,
                    recipient = searchResult.address.toBase58Instance(),
                    sourceToken = sourceToken,
                    feePayerToken = feePayerToken,
                    amount = if (sourceToken.isSOL) {
                        maxOf(rentExemptionInSol, sourceTokenAmount)
                    } else {
                        sourceTokenAmount
                    },
                )
            } catch (e: SendServiceTransactionError) {
                val feesInSol = FeeAmount(
                    transactionFee = BigInteger.ZERO,
                    accountCreationFee = rentExemptionInSol
                )
                return FeeCalculationState.PoolsNotFound(
                    feeInSol = FeeRelayerFee(
                        feesInSol = feesInSol,
                        feesInFeePayerToken = FeeAmount(
                            transactionFee = BigInteger.ZERO,
                            accountCreationFee = convertFromSol(feePayerToken, rentExemptionInSol)
                        ),
                        feesInSourceToken = FeeAmount(
                            transactionFee = BigInteger.ZERO,
                            accountCreationFee = convertFromSol(sourceToken, rentExemptionInSol)
                        ),
                        expectedFee = feesInSol
                    ),
                )
            }

            if (fees.tokenAccountRent.amount.amount.isZeroOrLess()) {
                return FeeCalculationState.NoFees
            }

            val networkFee = BigInteger.ZERO
            val accountCreationFeeInFeePayerToken = fees.tokenAccountRent.amount.amount

            val feesInSol = FeeAmount(
                transactionFee = networkFee,
                accountCreationFee = convertToSol(feePayerToken, accountCreationFeeInFeePayerToken)
            )

            val feesInFeePayerToken = FeeAmount(
                transactionFee = networkFee,
                accountCreationFee = accountCreationFeeInFeePayerToken
            )

            val feesInSourceToken = FeeAmount(
                transactionFee = networkFee,
                accountCreationFee = convertAmount(
                    from = feePayerToken,
                    to = sourceToken,
                    amount = accountCreationFeeInFeePayerToken
                )
            )

            return FeeCalculationState.Success(
                fee = FeeRelayerFee(
                    feesInSol = feesInSol,
                    feesInFeePayerToken = feesInFeePayerToken,
                    feesInSourceToken = feesInSourceToken,
                    expectedFee = feesInSol

                )
            )
        } catch (e: CancellationException) {
            Timber.i("Fee calculation cancelled")
            return FeeCalculationState.Cancelled
        } catch (e: Throwable) {
            Timber.i(e, "Failed to calculateFeesForFeeRelayer")
            return FeeCalculationState.Error(e)
        }
    }

    private suspend fun convertToSol(
        token: Token.Active,
        amount: BigInteger,
    ): BigInteger {
        if (token.isSOL) return amount
        return amountsConverter.convertAmount(
            amountFrom = token.mintAddressB58 to amount,
            mintsToConvertTo = listOf(Constants.WRAPPED_SOL_MINT.toBase58Instance())
        )[Constants.WRAPPED_SOL_MINT.toBase58Instance()] ?: BigInteger.ZERO
    }

    private suspend fun convertFromSol(
        token: Token.Active,
        amount: BigInteger,
    ): BigInteger {
        if (token.isSOL) return amount
        return amountsConverter.convertAmount(
            amountFrom = Constants.WRAPPED_SOL_MINT.toBase58Instance() to amount,
            mintsToConvertTo = listOf(token.mintAddressB58)
        )[token.mintAddressB58] ?: BigInteger.ZERO
    }

    private suspend fun convertAmount(
        from: Token.Active,
        to: Token.Active,
        amount: BigInteger,
    ): BigInteger {
        return amountsConverter.convertAmount(
            amountFrom = from.mintAddressB58 to amount,
            mintsToConvertTo = listOf(to.mintAddressB58)
        )[to.mintAddressB58] ?: BigInteger.ZERO
    }
}
