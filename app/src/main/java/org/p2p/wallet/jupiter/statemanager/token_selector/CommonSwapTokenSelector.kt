package org.p2p.wallet.jupiter.statemanager.token_selector

import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.token.findSolOrThrow
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.tokenservice.TokenServiceCoordinator

class CommonSwapTokenSelector(
    private val jupiterTokensRepository: JupiterSwapTokensRepository,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val selectedSwapTokenStorage: JupiterSwapStorageContract,
    private val dispatchers: CoroutineDispatchers,
) : SwapInitialTokenSelector {

    override suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel> = withContext(dispatchers.io) {
        val userTokensJob = async { tokenServiceCoordinator.getUserTokens() }
        val userTokens = userTokensJob.await()

        val userIsEmpty = userTokens.isEmpty() || userTokens.all { it.isZero }
        val userHasUsdc = userTokens.any { it.isUSDC }
        val userHasSol = userTokens.any { it.isSOL && !it.isZero }
        val userTokensContainsJupiter = jupiterTokensRepository.filterIntersectedTokens(userTokens)
        val userTokenWithMaxAmount = userTokensContainsJupiter.maxByOrNull { it.total }

        val tokenA: SwapTokenModel
        val tokenB: SwapTokenModel

        val savedSelectedTokenMintA = selectedSwapTokenStorage.savedTokenAMint
        val savedSelectedTokenMintB = selectedSwapTokenStorage.savedTokenBMint

        when {
            savedSelectedTokenMintA != null && savedSelectedTokenMintB != null -> {
                tokenA = findTokenByMint(userTokens, savedSelectedTokenMintA)
                tokenB = findTokenByMint(userTokens, savedSelectedTokenMintB)
            }
            userIsEmpty -> {
                val jupiterUsdcToSol = jupiterUsdcToJupiterSol()
                tokenA = SwapTokenModel.JupiterToken(jupiterUsdcToSol.first)
                tokenB = SwapTokenModel.JupiterToken(jupiterUsdcToSol.second)
            }
            userHasUsdc -> {
                tokenA = SwapTokenModel.UserToken(userTokens.first { it.isUSDC })
                tokenB = getTokenB(
                    jupiterTokensRepository = jupiterTokensRepository,
                    userTokens = userTokens,
                    preferSol = true,
                    savedSwapTokenB = savedSelectedTokenMintB
                )
            }
            userHasSol -> {
                tokenA = SwapTokenModel.UserToken(userTokens.findSolOrThrow())
                tokenB = getTokenB(
                    jupiterTokensRepository = jupiterTokensRepository,
                    userTokens = userTokens,
                    preferSol = false,
                    savedSwapTokenB = savedSelectedTokenMintB.takeIf { it != tokenA.mintAddress }
                )
            }
            userTokenWithMaxAmount != null -> {
                tokenA = SwapTokenModel.UserToken(userTokenWithMaxAmount)
                tokenB = getTokenB(
                    jupiterTokensRepository = jupiterTokensRepository,
                    userTokens = userTokens,
                    preferSol = true,
                    savedSwapTokenB = savedSelectedTokenMintB
                )
            }
            // no any tokens
            else -> {
                val jupiterUsdcToSol = jupiterUsdcToJupiterSol()
                tokenA = SwapTokenModel.JupiterToken(jupiterUsdcToSol.first)
                tokenB = SwapTokenModel.JupiterToken(jupiterUsdcToSol.second)
            }
        }

        selectedSwapTokenStorage.savedTokenAMint = tokenA.mintAddress
        selectedSwapTokenStorage.savedTokenBMint = tokenB.mintAddress
        tokenA to tokenB
    }

    private suspend fun jupiterUsdcToJupiterSol(): Pair<JupiterSwapToken, JupiterSwapToken> {
        return jupiterTokensRepository.requireUsdc() to jupiterTokensRepository.requireWrappedSol()
    }

    private suspend fun findTokenByMint(
        userTokens: List<Token.Active>,
        tokenMint: Base58String
    ): SwapTokenModel {
        return userTokens
            .firstOrNull { it.mintAddress == tokenMint.base58Value }
            ?.let(SwapTokenModel::UserToken)
            ?: jupiterTokensRepository.requireTokenByMint(tokenMint)
                .let(SwapTokenModel::JupiterToken)
    }
}
