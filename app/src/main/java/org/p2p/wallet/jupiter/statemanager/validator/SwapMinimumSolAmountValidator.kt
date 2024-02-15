package org.p2p.wallet.jupiter.statemanager.validator

import java.math.BigDecimal
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isZero
import org.p2p.core.utils.toLamports
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.statemanager.SwapFeatureException
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository

class SwapMinimumSolAmountValidator(
    private val rpcAmountRepository: RpcAmountRepository
) {

    /**
     * We need to leave the min required balance for SOL account.
     * It's about 0.0089088 SOL (890880 LAMPORTS)
     * */
    suspend fun validateMinimumSolAmount(solToken: SwapTokenModel.UserToken, newAmount: BigDecimal) {
        val minRentExemption = rpcAmountRepository.getMinBalanceForRentExemption(dataLength = 0)
        val totalInLamports = solToken.tokenAmountInLamports

        val newAmountInLamports = newAmount.toLamports(solToken.decimals)
        val remainingBalance = totalInLamports - newAmountInLamports

        if (remainingBalance.isZero() || remainingBalance >= minRentExemption) {
            return
        }

        throw SwapFeatureException.InsufficientSolBalance(
            inputAmount = newAmount,
            userSolToken = solToken,
            allowedAmount = (totalInLamports - minRentExemption).fromLamports(solToken.decimals)
        )
    }
}
