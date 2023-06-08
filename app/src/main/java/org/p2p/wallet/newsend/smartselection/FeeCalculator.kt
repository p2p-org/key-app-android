package org.p2p.wallet.newsend.smartselection

import timber.log.Timber
import java.math.BigInteger
import java.util.concurrent.CancellationException
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.interactor.FeeRelayerCalculationInteractor
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeeInSol
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository

private const val TAG = "FeeCalculator"

class FeeCalculator(
    private val feeRelayerCalculationInteractor: FeeRelayerCalculationInteractor,
    private val rpcAmountRepository: RpcAmountRepository
) {

    suspend fun calculateFee(
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        recipient: String,
        useCache: Boolean = true
    ): FeeCalculationState {
        try {
            val feeInSol = calculateFeeInSol(
                feePayerToken = feePayerToken,
                sourceToken = sourceToken,
                recipient = recipient,
                useCache = useCache
            )

            if (feeInSol.isFree) {
                return FeeCalculationState.NoFees
            }

            return calculateFeeInSpl(
                feePayerToken = feePayerToken,
                feeInSol = feeInSol
            )
        } catch (e: CancellationException) {
            Timber.tag(TAG).i("Fee calculation cancelled")
            return FeeCalculationState.Cancelled
        } catch (e: Throwable) {
            Timber.tag(TAG).i(e, "Failed to calculate fee")
            return FeeCalculationState.Failed(e)
        }
    }

    suspend fun getMinRentExemption(): BigInteger = rpcAmountRepository.getMinBalanceForRentExemption(0)

    private suspend fun calculateFeeInSpl(
        feePayerToken: Token.Active,
        feeInSol: FeeInSol
    ): FeeCalculationState {
        return feeRelayerCalculationInteractor.calculateFeesInSpl(
            feePayerToken = feePayerToken,
            feeInSol = feeInSol
        )
    }

    private suspend fun calculateFeeInSol(
        sourceToken: Token.Active,
        feePayerToken: Token.Active,
        recipient: String,
        useCache: Boolean = true
    ): FeeInSol {
        return feeRelayerCalculationInteractor.calculateFeesInSol(
            feePayerToken = feePayerToken,
            sourceToken = sourceToken,
            recipient = recipient,
            useCache = useCache
        )
    }
}
