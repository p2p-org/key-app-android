package org.p2p.wallet.jupiter.statemanager

import org.p2p.core.token.Token
import org.p2p.wallet.jupiter.interactor.JupiterSwapInteractor
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository

class SwapUserTokensChangeHandler constructor(
    private val swapInteractor: JupiterSwapInteractor,
    private val swapTokensRepository: JupiterSwapTokensRepository,
) {

    suspend fun handleUserTokensChange(currentState: SwapState, userTokens: List<Token.Active>): SwapState {
        // org/p2p/wallet/user/interactor/UserInteractor.kt:128
        // tokens cleared each time on update, emit empty list
        if (userTokens.isEmpty()) return currentState
        val (tokenA, tokenB) = swapInteractor.getSwapTokenPair(currentState)
        // SwapState.InitialLoading
        if (tokenA == null || tokenB == null) return currentState

        val updatedTokenA = userTokens.find { it.mintAddress == tokenA.mintAddress.base58Value }
        val jupiterTokenA = swapTokensRepository.getTokens()
            .find { it.tokenMint == tokenA.mintAddress }

        val updatedTokenB = userTokens.find { it.mintAddress == tokenB.mintAddress.base58Value }
        val jupiterTokenB = swapTokensRepository.getTokens()
            .find { it.tokenMint == tokenB.mintAddress }

        val isNewTokenAAmount = tokenA is SwapTokenModel.UserToken &&
            updatedTokenA?.totalInLamports != tokenA.tokenAmountInLamports

        var newState = currentState
        val updatedTokenAState = when {
            updatedTokenA == null && jupiterTokenA != null -> onUserTokenAGone(newState, jupiterTokenA)
            updatedTokenA != null && isNewTokenAAmount -> onUserTokenAChangeBalance(newState, updatedTokenA)
            else -> null
        }
        updatedTokenAState?.let { newState = updatedTokenAState }

        val isNewTokenBAmount = tokenB is SwapTokenModel.UserToken &&
            updatedTokenB?.totalInLamports != tokenB.tokenAmountInLamports
        val updatedTokenBState = when {
            updatedTokenB == null && jupiterTokenB != null -> onUserTokenBGone(newState, jupiterTokenB)
            updatedTokenB != null && isNewTokenBAmount -> onUserTokenBChangeBalance(newState, updatedTokenB)
            else -> null
        }
        updatedTokenBState?.let { newState = updatedTokenBState }
        return newState
    }

    private fun onUserTokenAChangeBalance(
        featureState: SwapState,
        newUserToken: Token.Active,
    ): SwapState? {
        val newUserSwapToken = SwapTokenModel.UserToken(newUserToken)
        val newState: SwapState? = when (featureState) {
            SwapState.InitialLoading -> featureState

            is SwapState.LoadingRoutes -> featureState.copy(tokenA = newUserSwapToken)
            is SwapState.LoadingTransaction -> featureState.copy(tokenA = newUserSwapToken)
            is SwapState.SwapLoaded -> featureState.copy(tokenA = newUserSwapToken)
            is SwapState.TokenANotZero -> featureState.copy(tokenA = newUserSwapToken)
            is SwapState.TokenAZero -> featureState.copy(tokenA = newUserSwapToken)
            is SwapState.RoutesLoaded -> featureState.copy(tokenA = newUserSwapToken)

            is SwapState.SwapException -> onUserTokenAChangeBalance(featureState.previousFeatureState, newUserToken)
        }
        return newState
    }

    private fun onUserTokenAGone(
        featureState: SwapState,
        newJupiterToken: JupiterSwapToken,
    ): SwapState? {
        val newJupiterSwapToken = SwapTokenModel.JupiterToken(newJupiterToken)
        val newState: SwapState? = when (featureState) {
            SwapState.InitialLoading -> featureState

            is SwapState.LoadingRoutes -> featureState.copy(tokenA = newJupiterSwapToken)
            is SwapState.LoadingTransaction -> featureState.copy(tokenA = newJupiterSwapToken)
            is SwapState.SwapLoaded -> featureState.copy(tokenA = newJupiterSwapToken)
            is SwapState.TokenANotZero -> featureState.copy(tokenA = newJupiterSwapToken)
            is SwapState.TokenAZero -> featureState.copy(tokenA = newJupiterSwapToken)
            is SwapState.RoutesLoaded -> featureState.copy(tokenA = newJupiterSwapToken)

            is SwapState.SwapException -> onUserTokenAGone(featureState.previousFeatureState, newJupiterToken)
        }
        return newState
    }

    private fun onUserTokenBChangeBalance(
        featureState: SwapState,
        newUserToken: Token.Active,
    ): SwapState? {
        val newUserSwapToken = SwapTokenModel.UserToken(newUserToken)
        val newState: SwapState? = when (featureState) {
            SwapState.InitialLoading -> featureState

            is SwapState.LoadingRoutes -> featureState.copy(tokenB = newUserSwapToken)
            is SwapState.LoadingTransaction -> featureState.copy(tokenB = newUserSwapToken)
            is SwapState.SwapLoaded -> featureState.copy(tokenB = newUserSwapToken)
            is SwapState.TokenANotZero -> featureState.copy(tokenB = newUserSwapToken)
            is SwapState.TokenAZero -> featureState.copy(tokenB = newUserSwapToken)
            is SwapState.RoutesLoaded -> featureState.copy(tokenB = newUserSwapToken)

            is SwapState.SwapException -> onUserTokenAChangeBalance(featureState.previousFeatureState, newUserToken)
        }
        return newState
    }

    private fun onUserTokenBGone(
        featureState: SwapState,
        newJupiterToken: JupiterSwapToken,
    ): SwapState? {
        val newJupiterSwapToken = SwapTokenModel.JupiterToken(newJupiterToken)
        val newState: SwapState? = when (featureState) {
            SwapState.InitialLoading -> featureState

            is SwapState.LoadingRoutes -> featureState.copy(tokenB = newJupiterSwapToken)
            is SwapState.LoadingTransaction -> featureState.copy(tokenB = newJupiterSwapToken)
            is SwapState.SwapLoaded -> featureState.copy(tokenB = newJupiterSwapToken)
            is SwapState.TokenANotZero -> featureState.copy(tokenB = newJupiterSwapToken)
            is SwapState.TokenAZero -> featureState.copy(tokenB = newJupiterSwapToken)
            is SwapState.RoutesLoaded -> featureState.copy(tokenB = newJupiterSwapToken)

            is SwapState.SwapException -> onUserTokenAGone(featureState.previousFeatureState, newJupiterToken)
        }
        return newState
    }
}
