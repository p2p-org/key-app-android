package org.p2p.wallet.jupiter.statemanager.token_selector

import org.p2p.core.crypto.Base58String
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken

data class SwapInitialTokensData(
    val token: Token.Active?,
    val tokenAMint: Base58String?,
    val tokenBMint: Base58String?
) {
    companion object {
        val NO_DATA = SwapInitialTokensData(token = null, tokenAMint = null, tokenBMint = null)
    }
}

interface SwapInitialTokenSelector {

    suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel>

    fun getTokenB(
        jupiterTokens: List<JupiterSwapToken>,
        userTokens: List<Token.Active>,
        preferSol: Boolean,
        savedSwapTokenB: Base58String?
    ): SwapTokenModel {
        return when {
            savedSwapTokenB != null -> {
                val savedSelectedUserToken = userTokens.firstOrNull { it.mintAddress == savedSwapTokenB.base58Value }
                val savedSelectedJupiterToken = jupiterTokens.first { it.tokenMint == savedSwapTokenB }
                if (savedSelectedUserToken != null) {
                    SwapTokenModel.UserToken(savedSelectedUserToken)
                } else {
                    SwapTokenModel.JupiterToken(savedSelectedJupiterToken)
                }
            }
            preferSol -> {
                val userSol = userTokens.firstOrNull { it.isSOL }
                val jupiterSol = jupiterTokens.first { it.tokenMint.base58Value == Constants.WRAPPED_SOL_MINT }
                if (userSol != null) {
                    SwapTokenModel.UserToken(userSol)
                } else {
                    SwapTokenModel.JupiterToken(jupiterSol)
                }
            }
            else -> {
                val userUsdc = userTokens.firstOrNull { it.isUSDC }
                val jupiterUsdc = jupiterTokens.first { it.tokenSymbol == Constants.USDC_SYMBOL }
                if (userUsdc != null) {
                    SwapTokenModel.UserToken(userUsdc)
                } else {
                    SwapTokenModel.JupiterToken(jupiterUsdc)
                }
            }
        }
    }
}
