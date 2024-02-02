package org.p2p.wallet.jupiter.interactor

import org.p2p.core.crypto.Base58String
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.jupiter.statemanager.SwapStateAction
import org.p2p.wallet.jupiter.statemanager.SwapStateManager
import org.p2p.wallet.jupiter.ui.tokens.SwapTokensListMode
import org.p2p.wallet.tokenservice.TokenServiceCoordinator

class SwapTokensInteractor(
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val swapTokensRepository: JupiterSwapTokensRepository,
    private val swapStateManager: SwapStateManager,
    private val jupiterSwapInteractor: JupiterSwapInteractor,
) {
    suspend fun requireCurrentTokenA(): SwapTokenModel {
        return swapStateManager.getStateValue { state ->
            requireNotNull(jupiterSwapInteractor.getSwapTokenPair(state)?.first) {
                "Illegal swap state, can't find selected token A for the list: $state"
            }
        }
    }

    suspend fun requireCurrentTokenB(): SwapTokenModel {
        return swapStateManager.getStateValue { state ->
            requireNotNull(jupiterSwapInteractor.getSwapTokenPair(state)?.second) {
                "Illegal swap state, can't find selected token B for the list: $state"
            }
        }
    }

    suspend fun getAllTokens(): List<SwapTokenModel> {
        val userTokens = tokenServiceCoordinator.getUserTokens().map(SwapTokenModel::UserToken)
        val userTokensMints = userTokens.map { it.mintAddress }.toSet()
        val jupiterTokens = swapTokensRepository
            .findTokensExcludingMints(userTokensMints)
            .map(SwapTokenModel::JupiterToken)

        return userTokens + jupiterTokens
    }

    suspend fun getAllTokensA(): List<SwapTokenModel> {
        val tokenA = requireCurrentTokenA()
        return getAllTokens().filter { it.notSelectedToken(tokenA) }
    }

    suspend fun getAllAvailableTokensB(): List<SwapTokenModel> {
        val userTokens = tokenServiceCoordinator.getUserTokens().map(SwapTokenModel::UserToken)
        val userTokensMints = userTokens.map { it.mintAddress }.toSet()

        val tokenA = requireCurrentTokenA()
        val tokenB = requireCurrentTokenB()

        val availableTokenBMints = swapTokensRepository.getSwappableTokens(sourceTokenMint = tokenA.mintAddress)
            .map(SwapTokenModel::JupiterToken)
            .filter { it.mintAddress !in userTokensMints }

        val allTokensB = userTokens + availableTokenBMints
        return allTokensB.filter { it.notSelectedToken(tokenB) }
    }

    suspend fun searchToken(tokenMode: SwapTokensListMode, symbolOrName: String): List<SwapTokenModel> {
        val userTokens = tokenServiceCoordinator.getUserTokens().map(SwapTokenModel::UserToken)
        val userTokensMints = userTokens.map { it.mintAddress }.toSet()
        val filteredUserTokens = filterUserSwapTokens(userTokens, symbolOrName)

        val selectedToken = when (tokenMode) {
            SwapTokensListMode.TOKEN_A -> requireCurrentTokenA()
            SwapTokensListMode.TOKEN_B -> requireCurrentTokenB()
        }

        val searchedJupiterTokens = swapTokensRepository.searchTokens(
            mintAddressOrSymbol = symbolOrName
        )
            .map(SwapTokenModel::JupiterToken)
            .filter { it.mintAddress !in userTokensMints }

        return filteredUserTokens
            .plus(searchedJupiterTokens)
            .filter { it.notSelectedToken(selectedToken) }
    }

    private fun filterUserSwapTokens(swapTokens: List<SwapTokenModel>, query: String): List<SwapTokenModel> {
        val filteredList = mutableListOf<SwapTokenModel>()

        // Filter items that start with the query
        swapTokens.filterTo(filteredList) {
            it.tokenSymbol.startsWith(query, ignoreCase = true) || it.tokenName.startsWith(query, ignoreCase = true)
        }

        // Filter items that contain the query
        swapTokens.filterTo(filteredList) {
            it.tokenSymbol.contains(query, ignoreCase = true) && !it.tokenSymbol.startsWith(query, ignoreCase = true) ||
                it.tokenName.contains(query, ignoreCase = true) && !it.tokenName.startsWith(query, ignoreCase = true)
        }

        // Filter items that match the mint address in query
        swapTokens.filterTo(filteredList) {
            it.mintAddress.base58Value.startsWith(query, ignoreCase = true) ||
                it.mintAddress.base58Value.contentEquals(query)
        }

        return filteredList
    }

    private fun SwapTokenModel.notSelectedToken(selectedTokenMint: SwapTokenModel): Boolean {
        return this.mintAddress != selectedTokenMint.mintAddress
    }

    suspend fun findTokenByMintAddress(mintAddress: Base58String): SwapTokenModel.JupiterToken? {
        return swapTokensRepository.findTokenByMint(mintAddress)
            ?.let { SwapTokenModel.JupiterToken(it) }
    }

    fun selectToken(mode: SwapTokensListMode, token: SwapTokenModel) {
        val action = when (mode) {
            SwapTokensListMode.TOKEN_A -> SwapStateAction.TokenAChanged(token)
            SwapTokensListMode.TOKEN_B -> SwapStateAction.TokenBChanged(token)
        }
        swapStateManager.onNewAction(action)
    }
}
