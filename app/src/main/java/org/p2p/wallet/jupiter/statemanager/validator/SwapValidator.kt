package org.p2p.wallet.jupiter.statemanager.validator

import java.math.BigDecimal
import org.p2p.core.utils.isMoreThan
import org.p2p.core.utils.isZero
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.statemanager.SwapFeatureException

class SwapValidator {

    fun validateInputAmount(
        tokenA: SwapTokenModel,
        amountTokenA: BigDecimal
    ) {
        when {
            !isValidInputAmount(tokenA, amountTokenA) ->
                throw SwapFeatureException.NotValidTokenA(amountTokenA)
        }
    }

    fun isValidInputAmount(
        tokenA: SwapTokenModel,
        amountTokenA: BigDecimal
    ): Boolean {
        val isJupiterTokenAndNotZero = tokenA is SwapTokenModel.JupiterToken && !amountTokenA.isZero()
        val isUserTokenAndNotEnough = tokenA is SwapTokenModel.UserToken && amountTokenA.isMoreThan(tokenA.tokenAmount)
        return !(isJupiterTokenAndNotZero || isUserTokenAndNotEnough)
    }

    fun validateIsSameTokens(
        tokenA: SwapTokenModel,
        tokenB: SwapTokenModel,
    ) {
        if (tokenA.mintAddress == tokenB.mintAddress) throw SwapFeatureException.SameTokens
    }
}
