package org.p2p.wallet.send.smartselection

import timber.log.Timber
import java.util.concurrent.CancellationException
import org.p2p.core.token.Token
import org.p2p.wallet.feerelayer.interactor.FeeRelayerCalculationInteractor
import org.p2p.wallet.feerelayer.model.FeeCalculationState
import org.p2p.wallet.feerelayer.model.FeeInSol

private const val TAG = "FeeCalculator"

class FeeCalculator(
    private val feeRelayerCalculationInteractor: FeeRelayerCalculationInteractor
) {

    suspend fun calculateFee(
        feePayerToken: Token.Active,
        token: Token.Active,
        recipient: String,
        useCache: Boolean = true
    ): FeeCalculationState {
        try {
            val feeInSol = calculateFeeInSol(
                feePayerToken = feePayerToken,
                token = token,
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
        feePayerToken: Token.Active,
        token: Token.Active,
        recipient: String,
        useCache: Boolean = true
    ): FeeInSol {
        return feeRelayerCalculationInteractor.calculateFeesInSol(
            feePayerToken = feePayerToken,
            token = token,
            recipient = recipient,
            useCache = useCache
        )
    }
}
