package org.p2p.wallet.swap.jupiter.statemanager.token_selector

import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.core.token.findSolOrThrow
import org.p2p.core.utils.Constants.USDC_SYMBOL
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.utils.Base58String

class CommonSwapTokenSelector(
    private val jupiterTokensRepository: JupiterSwapTokensRepository,
    private val homeLocalRepository: HomeLocalRepository,
    private val selectedSwapTokenStorage: JupiterSwapStorageContract,
    private val dispatchers: CoroutineDispatchers,
) : SwapInitialTokenSelector {

    override suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel> = withContext(dispatchers.io) {
        val jupiterTokensJob = async { jupiterTokensRepository.getTokens() }
        val userTokensJob = async { homeLocalRepository.getUserTokens() }
        val jupiterTokens = jupiterTokensJob.await()
        val userTokens = userTokensJob.await()

        val userHasUsdc = userTokens.any { it.isUSDC }
        val userHasSol = userTokens.any { it.isSOL && !it.isZero }
        val jupiterMints = jupiterTokens.map { it.tokenMint.base58Value }
        val userTokensContainsJupiter = userTokens.filter {
            jupiterMints.contains(it.mintAddress) && !it.isZero
        }
        val userTokenWithMaxAmount = userTokensContainsJupiter.maxByOrNull { it.total }

        val tokenA: SwapTokenModel
        val tokenB: SwapTokenModel

        val savedSelectedTokenMintA = selectedSwapTokenStorage.savedTokenAMint
        val savedSelectedTokenMintB = selectedSwapTokenStorage.savedTokenBMint

        when {
            savedSelectedTokenMintA != null && savedSelectedTokenMintB != null -> {
                tokenA = findTokenByMint(userTokens, jupiterTokens, savedSelectedTokenMintA)
                tokenB = findTokenByMint(userTokens, jupiterTokens, savedSelectedTokenMintB)
            }
            userHasUsdc -> {
                tokenA = SwapTokenModel.UserToken(userTokens.first { it.isUSDC })
                tokenB = getTokenB(
                    jupiterTokens = jupiterTokens,
                    userTokens = userTokens,
                    preferSol = true,
                    savedSwapTokenB = savedSelectedTokenMintB
                )
            }
            userHasSol -> {
                tokenA = SwapTokenModel.UserToken(userTokens.findSolOrThrow())
                tokenB = getTokenB(
                    jupiterTokens = jupiterTokens,
                    userTokens = userTokens,
                    preferSol = false,
                    savedSwapTokenB = savedSelectedTokenMintB
                )
            }
            userTokenWithMaxAmount != null -> {
                tokenA = SwapTokenModel.UserToken(userTokenWithMaxAmount)
                tokenB = getTokenB(
                    jupiterTokens = jupiterTokens,
                    userTokens = userTokens,
                    preferSol = true,
                    savedSwapTokenB = savedSelectedTokenMintB
                )
            }
            // no any tokens
            else -> {
                val jupiterUsdc = jupiterTokens.first { it.tokenSymbol == USDC_SYMBOL }
                val jupiterSol = jupiterTokens.first { it.tokenMint.base58Value == WRAPPED_SOL_MINT }
                tokenA = SwapTokenModel.JupiterToken(jupiterUsdc)
                tokenB = SwapTokenModel.JupiterToken(jupiterSol)
            }
        }

        selectedSwapTokenStorage.savedTokenAMint = tokenA.mintAddress
        selectedSwapTokenStorage.savedTokenBMint = tokenB.mintAddress
        tokenA to tokenB
    }

    private fun findTokenByMint(
        userTokens: List<Token.Active>,
        jupiterTokens: List<JupiterSwapToken>,
        tokenMint: Base58String
    ): SwapTokenModel {
        return userTokens
            .firstOrNull { it.mintAddress == tokenMint.base58Value }
            ?.let(SwapTokenModel::UserToken)
            ?: jupiterTokens
                .first { it.tokenMint == tokenMint }
                .let(SwapTokenModel::JupiterToken)
    }
}
