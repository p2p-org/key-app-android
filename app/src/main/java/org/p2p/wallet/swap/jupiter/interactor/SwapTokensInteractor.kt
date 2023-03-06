package org.p2p.wallet.swap.jupiter.interactor

import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.routes.JupiterSwapRoutesRepository
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.swap.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.swap.ui.jupiter.tokens.SwapTokensListMode

class SwapTokensInteractor(
    private val homeLocalRepository: HomeLocalRepository,
    private val swapTokensRepository: JupiterSwapTokensRepository,
    private val swapRoutesRepository: JupiterSwapRoutesRepository,
    private val swapStateManager: SwapStateManager
) {
    suspend fun getCurrentTokenA(): SwapTokenModel {
        return swapStateManager.getStateValue { state ->
            when (state) {
                is SwapState.TokenAZero -> state.tokenA
                is SwapState.LoadingRoutes -> state.tokenA
                is SwapState.LoadingTransaction -> state.tokenA
                is SwapState.SwapLoaded -> state.tokenA
                else -> error("Illegal swap state, can't find selected token A for the list: $state")
            }
        }
    }

    suspend fun getCurrentTokenB(): SwapTokenModel {
        return swapStateManager.getStateValue { state ->
            when (state) {
                is SwapState.TokenAZero -> state.tokenB
                is SwapState.LoadingRoutes -> state.tokenB
                is SwapState.LoadingTransaction -> state.tokenB
                is SwapState.SwapLoaded -> state.tokenB
                else -> error("Illegal swap state, can't find selected token B for the list: $state")
            }
        }
    }

    suspend fun getAllTokensA(): List<SwapTokenModel> {
        val userTokens = homeLocalRepository.getUserTokens()
        val jupiterTokens = swapTokensRepository.getTokens()
        val tokenA = getCurrentTokenA()

        val userTokensModel = userTokens.map(SwapTokenModel::UserToken)
        val jupiterTokensModel = jupiterTokens.map(SwapTokenModel::JupiterToken)
            .filter { it.mintAddress !in userTokensModel.map(SwapTokenModel.UserToken::mintAddress) }
        val allTokens = userTokensModel + jupiterTokensModel

        return allTokens.filter { it.notSelectedToken(tokenA) }
    }

    suspend fun getAllAvailableTokensB(): List<SwapTokenModel> {
        val userTokens = homeLocalRepository.getUserTokens().map(SwapTokenModel::UserToken)
        val jupiterTokens = swapTokensRepository.getTokens().map(SwapTokenModel::JupiterToken)
            .filter { it.mintAddress !in userTokens.map(SwapTokenModel.UserToken::mintAddress) }

        val tokenA = getCurrentTokenA()
        val tokenB = getCurrentTokenB()
        val availableTokenBMints = swapRoutesRepository.getSwappableTokenMints(sourceTokenMint = tokenA.mintAddress)

        val allTokensB = userTokens + jupiterTokens
        return allTokensB
            .filter { it.notSelectedToken(tokenB) && it.mintAddress in availableTokenBMints }
    }

    suspend fun searchToken(tokenMode: SwapTokensListMode, symbolOrName: String): List<SwapTokenModel> {
        val tokens = when (tokenMode) {
            SwapTokensListMode.TOKEN_A -> getAllTokensA()
            SwapTokensListMode.TOKEN_B -> getAllAvailableTokensB()
        }

        return filterSwapTokens(tokens, symbolOrName)
    }

    private fun filterSwapTokens(swapTokens: List<SwapTokenModel>, query: String): List<SwapTokenModel> {
        val filteredList = mutableListOf<SwapTokenModel>()

        // Filter items that start with the query
        swapTokens.filterTo(filteredList) {
            it.tokenSymbol.startsWith(query, ignoreCase = true)
        }

        // Filter items that contain the query
        swapTokens.filterTo(filteredList) {
            it.tokenSymbol.contains(query, ignoreCase = true) && !it.tokenSymbol.startsWith(query, ignoreCase = true)
        }

        return filteredList
    }

    private fun SwapTokenModel.notSelectedToken(selectedTokenMint: SwapTokenModel): Boolean {
        return this.mintAddress != selectedTokenMint.mintAddress
    }

    fun selectToken(mode: SwapTokensListMode, token: SwapTokenModel) {
        val action = when (mode) {
            SwapTokensListMode.TOKEN_A -> SwapStateAction.TokenAChanged(token)
            SwapTokensListMode.TOKEN_B -> SwapStateAction.TokenBChanged(token)
        }
        swapStateManager.onNewAction(action)
    }
}
