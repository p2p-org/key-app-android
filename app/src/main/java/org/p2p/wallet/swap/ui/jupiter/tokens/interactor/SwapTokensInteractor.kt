package org.p2p.wallet.swap.ui.jupiter.tokens.interactor

import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.routes.JupiterSwapRoutesRepository
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.utils.Base58String

class SwapTokensInteractor(
    private val homeLocalRepository: HomeLocalRepository,
    private val swapTokensRepository: JupiterSwapTokensRepository,
    private val swapRoutesRepository: JupiterSwapRoutesRepository,
//    private val swapStateManager: SwapStateManager
) {
    suspend fun getCurrentTokenA(): SwapTokenModel {
        return SwapTokenModel.UserToken(
            homeLocalRepository.getUserTokens().last()
        )
//        return swapStateManager.getStateValue { state ->
//            when (state) {
//                is SwapState.TokenAZero -> state.tokenA
//                is SwapState.LoadingRoutes -> state.tokenA
//                is SwapState.LoadingTransaction -> state.tokenA
//                is SwapState.SwapLoaded -> state.tokenA
//                else -> error("Illegal swap state, can't find selected token A for the list: $state")
//            }
//        }
    }

    suspend fun getAllTokensA(): List<SwapTokenModel> {
        val userTokens = homeLocalRepository.getUserTokens()
        val jupiterTokens = swapTokensRepository.getTokens()
        val tokenA = getCurrentTokenA()

        val userTokensModel = userTokens.map(SwapTokenModel::UserToken)
        val jupiterTokensModel = jupiterTokens.map(SwapTokenModel::JupiterToken)
        val allTokens = userTokensModel + jupiterTokensModel

        return allTokens.withoutCurrentToken(tokenA.mintAddress)
    }

    private fun List<SwapTokenModel>.withoutCurrentToken(chosenTokenMint: Base58String): List<SwapTokenModel> {
        val currentTokenIndex = indexOfFirst { it.mintAddress == chosenTokenMint }
        return toMutableList().apply { removeAt(currentTokenIndex) }
    }
}
