package org.p2p.wallet.swap.jupiter.statemanager.token_selector

import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.core.utils.Constants.USDC_SYMBOL
import org.p2p.core.utils.Constants.WRAPPED_SOL_MINT
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository

class CommonSwapTokenSelector(
    private val jupiterTokensRepository: JupiterSwapTokensRepository,
    private val homeLocalRepository: HomeLocalRepository,
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

        when {
            userHasUsdc -> {
                tokenA = SwapTokenModel.UserToken(userTokens.first { it.isUSDC })
                tokenB = getTokenB(
                    jupiterTokens = jupiterTokens,
                    userTokens = userTokens,
                    findSolOrUsdc = true
                )
            }
            userHasSol -> {
                tokenA = SwapTokenModel.UserToken(userTokens.first { it.isSOL })
                tokenB = getTokenB(
                    jupiterTokens = jupiterTokens,
                    userTokens = userTokens,
                    findSolOrUsdc = false
                )
            }
            userTokenWithMaxAmount != null -> {
                tokenA = SwapTokenModel.UserToken(userTokenWithMaxAmount)
                tokenB = getTokenB(
                    jupiterTokens = jupiterTokens,
                    userTokens = userTokens,
                    findSolOrUsdc = false
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
        tokenA to tokenB
    }
}
