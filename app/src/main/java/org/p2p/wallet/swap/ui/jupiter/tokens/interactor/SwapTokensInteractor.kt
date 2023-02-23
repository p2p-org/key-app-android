package org.p2p.wallet.swap.ui.jupiter.tokens.interactor

import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.routes.JupiterSwapRoutesRepository
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.swap.ui.jupiter.tokens.presenter.SwapTokensListMode
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

    suspend fun getCurrentTokenB(): SwapTokenModel {
        return SwapTokenModel.UserToken(
            homeLocalRepository.getUserTokens().last()
        )
//        return swapStateManager.getStateValue { state ->
//            when (state) {
//                is SwapState.TokenAZero -> state.tokenB
//                is SwapState.LoadingRoutes -> state.tokenB
//                is SwapState.LoadingTransaction -> state.tokenB
//                is SwapState.SwapLoaded -> state.tokenB
//                else -> error("Illegal swap state, can't find selected token B for the list: $state")
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

    suspend fun getAllAvailableTokensB(): List<SwapTokenModel> {
        val userTokens = homeLocalRepository.getUserTokens().map(SwapTokenModel::UserToken)
        val jupiterTokens = swapTokensRepository.getTokens().map(SwapTokenModel::JupiterToken)

        val tokenA = getCurrentTokenA()
        val tokenB = getCurrentTokenB()
        val availableTokenBMints = swapRoutesRepository.getSwappableTokenMints(sourceTokenMint = tokenA.mintAddress)

        val allTokensB = userTokens + jupiterTokens
        return allTokensB
            .withoutCurrentToken(tokenB.mintAddress)
            .filter { it.mintAddress in availableTokenBMints }
    }

    suspend fun searchToken(tokenMode: SwapTokensListMode, symbolOrName: String): List<SwapTokenModel> {
        return when (tokenMode) {
            SwapTokensListMode.TOKEN_A -> getAllTokensA()
            SwapTokensListMode.TOKEN_B -> getAllAvailableTokensB()
        }
            .filter { filterBySymbolOrName(swapToken = it, querySymbolOrName = symbolOrName) }
    }

    private fun filterBySymbolOrName(swapToken: SwapTokenModel, querySymbolOrName: String): Boolean {
        val (symbol, name) = when (swapToken) {
            is SwapTokenModel.JupiterToken -> swapToken.details.tokenSymbol to swapToken.details.tokenName
            is SwapTokenModel.UserToken -> swapToken.details.tokenSymbol to swapToken.details.tokenName
        }
        return symbol.startsWith(querySymbolOrName, ignoreCase = true) ||
            name.startsWith(querySymbolOrName, ignoreCase = true)
    }

    private fun List<SwapTokenModel>.withoutCurrentToken(chosenTokenMint: Base58String): List<SwapTokenModel> {
        val currentTokenIndex = indexOfFirst { it.mintAddress == chosenTokenMint }
        return toMutableList().apply { removeAt(currentTokenIndex) }
    }
}
