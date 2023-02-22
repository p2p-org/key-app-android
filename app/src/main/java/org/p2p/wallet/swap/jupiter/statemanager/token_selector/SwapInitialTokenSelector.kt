package org.p2p.wallet.swap.jupiter.statemanager.token_selector

import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken

interface SwapInitialTokenSelector {

    suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel>

    fun getTokenB(
        jupiterTokens: List<JupiterSwapToken>,
        userTokens: List<Token.Active>,
        findSolOrUsdc: Boolean,
    ): SwapTokenModel =
        if (findSolOrUsdc) {
            val userSol = userTokens.firstOrNull { it.isSOL }
            val jupiterSol = jupiterTokens.first { it.tokenMint.base58Value == Constants.WRAPPED_SOL_MINT }
            if (userSol != null) {
                SwapTokenModel.UserToken(userSol)
            } else {
                SwapTokenModel.JupiterToken(jupiterSol)
            }
        } else {
            val userUsdc = userTokens.firstOrNull { it.isUSDC }
            val jupiterUsdc = jupiterTokens.first { it.tokenSymbol == Constants.USDC_SYMBOL }
            if (userUsdc != null) {
                SwapTokenModel.UserToken(userUsdc)
            } else {
                SwapTokenModel.JupiterToken(jupiterUsdc)
            }
        }
}
