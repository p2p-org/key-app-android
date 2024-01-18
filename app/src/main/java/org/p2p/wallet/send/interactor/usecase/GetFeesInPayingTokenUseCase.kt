package org.p2p.wallet.send.interactor.usecase

import timber.log.Timber
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.solanaj.core.FeeAmount
import org.p2p.wallet.feerelayer.interactor.FeeRelayerInteractor
import org.p2p.wallet.feerelayer.model.FeePoolsState

class GetFeesInPayingTokenUseCase(
    private val feeRelayerInteractor: FeeRelayerInteractor
) {

    suspend fun executeNullable(
        token: Token.Active,
        transactionFeeInSOL: BigInteger,
        accountCreationFeeInSOL: BigInteger
    ): Pair<String, FeeAmount>? = try {
        Timber.i("getFeesInPayingTokenNullable for ${token.mintAddress}")
        val feeInSpl = execute(
            feePayerToken = token,
            transactionFeeInSOL = transactionFeeInSOL,
            accountCreationFeeInSOL = accountCreationFeeInSOL
        )

        if (feeInSpl is FeePoolsState.Calculated) {
            Timber.i("Fees found for ${token.mintAddress}")
            token.tokenSymbol to feeInSpl.feeInSpl
        } else {
            Timber.i("Fees are null found for ${token.mintAddress}")
            null
        }
    } catch (e: IllegalStateException) {
        Timber.i(e, "No fees found for token ${token.mintAddress}")
        // ignoring tokens without fees
        null
    }

    suspend fun execute(
        feePayerToken: Token.Active,
        transactionFeeInSOL: BigInteger,
        accountCreationFeeInSOL: BigInteger
    ): FeePoolsState {
        Timber.i("Fetching fees in paying token: ${feePayerToken.mintAddress}")
        if (feePayerToken.isSOL) {
            val fee = FeeAmount(
                transaction = transactionFeeInSOL,
                accountBalances = accountCreationFeeInSOL
            )
            return FeePoolsState.Calculated(fee)
        }

        return feeRelayerInteractor.calculateFeeInPayingToken(
            feeInSOL = FeeAmount(
                transaction = transactionFeeInSOL,
                accountBalances = accountCreationFeeInSOL
            ),
            payingFeeTokenMint = feePayerToken.mintAddress
        )
    }
}
