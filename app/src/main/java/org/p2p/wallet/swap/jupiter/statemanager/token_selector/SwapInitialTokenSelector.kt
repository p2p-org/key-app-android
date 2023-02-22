package org.p2p.wallet.swap.jupiter.statemanager.token_selector

import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken

interface SwapInitialTokenSelector {

    suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel>
}

fun jupiterSwapGetTokenB(
    jupiterTokens: List<JupiterSwapToken>,
    userTokens: List<Token.Active>,
    findSolOrUSDC: Boolean,
): SwapTokenModel =
    when {
        findSolOrUSDC -> {
            val userSol = userTokens.firstOrNull { it.isSOL }
            val jupiterSol = jupiterTokens.first { it.tokenMint.base58Value == Constants.WRAPPED_SOL_MINT }
            if (userSol != null) {
                SwapTokenModel.UserToken(userSol)
            } else {
                SwapTokenModel.JupiterToken(jupiterSol)
            }
        }
        else -> {
            val userUSDC = userTokens.firstOrNull { it.isUSDC }
            val jupiterUSDC = jupiterTokens.first { it.tokenSymbol == Constants.USDC_SYMBOL }
            if (userUSDC != null) {
                SwapTokenModel.UserToken(userUSDC)
            } else {
                SwapTokenModel.JupiterToken(jupiterUSDC)
            }
        }
    }
