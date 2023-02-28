package org.p2p.wallet.swap.jupiter.statemanager.validator

import java.math.BigDecimal
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.isZero
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.statemanager.SwapFeatureException

class SwapValidator {

    fun validateInputAmount(
        tokenA: SwapTokenModel,
        amountTokenA: BigDecimal
    ) {
        when {
            tokenA is SwapTokenModel.JupiterToken && !amountTokenA.isZero() ||
                tokenA is SwapTokenModel.UserToken && amountTokenA.isMoreThan(tokenA.tokenAmount)
            -> throw SwapFeatureException.NotValidTokenA(amountTokenA)
        }
    }

    fun validateIsSameTokens(
        tokenA: SwapTokenModel,
        tokenB: SwapTokenModel,
    ) {
        if (tokenA.mintAddress == tokenB.mintAddress) throw SwapFeatureException.SameTokens
    }
}
