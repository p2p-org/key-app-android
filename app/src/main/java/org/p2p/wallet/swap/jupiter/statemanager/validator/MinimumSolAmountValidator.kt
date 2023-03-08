package org.p2p.wallet.swap.jupiter.statemanager.validator

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.utils.isLessThan
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.toLamports
import org.p2p.wallet.rpc.repository.amount.RpcAmountRepository
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.statemanager.SwapFeatureException
import org.p2p.wallet.swap.model.Slippage

class MinimumSolAmountValidator(
    private val rpcAmountRepository: RpcAmountRepository
) {

    suspend fun validateMinimumSolAmount(
        tokenA: SwapTokenModel,
        newAmount: BigDecimal,
        slippage: Slippage
    ) {

        if (!tokenA.isSol()) return

        val minRentExemption = rpcAmountRepository.getMinBalanceForRentExemption(0)

        val totalInLamports = when (tokenA) {
            is SwapTokenModel.UserToken -> tokenA.tokenAmountInLamports
            is SwapTokenModel.JupiterToken -> return
        }

        val newAmountInLamports = newAmount.toLamports(tokenA.decimals)

        val allowedAmountChange = (slippage.doubleValue * newAmountInLamports.toLong()) / 100

        /**
         * We need to leave the min required balance for SOL account.
         * It's about 0.0089088 SOL (890880 LAMPORTS)
         *
         * But in swap operations, we have a risk of slippage value.
         * We are adding it to the amount needed to be left on the account.
         * */
        val amountNeeded = newAmountInLamports + BigInteger.valueOf(allowedAmountChange.toLong())

        val remainingBalance = totalInLamports - amountNeeded

        if (remainingBalance.isMoreThan(BigInteger.ZERO) && remainingBalance.isLessThan(minRentExemption)) {
            throw SwapFeatureException.InsufficientSolBalance(newAmount)
        }
    }
}
