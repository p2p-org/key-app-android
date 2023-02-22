package org.p2p.wallet.swap.jupiter.statemanager.token_selector

import org.p2p.core.utils.Constants
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class CommonSwapTokenSelector(
    private val jupiterTokensRepository: JupiterSwapTokensRepository,
    private val homeLocalRepository: HomeLocalRepository,
    private val dispatchers: CoroutineDispatchers,
) : InitialTokenSelector {

    override suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel> = withContext(dispatchers.io) {
        val jupiterTokensJob = async { jupiterTokensRepository.getTokens() }
        val userTokensJob = async { homeLocalRepository.getUserTokens() }
        val jupiterTokens = jupiterTokensJob.await()
        val userTokens = userTokensJob.await()
        var haveUCDC = false
        var haveSOL = false
        userTokens.forEach {
            if (it.isUSDC) haveUCDC = true
            if (it.isSOL) haveSOL = true
        }
        val jupiterMints = jupiterTokens.map { it.tokenMint.base58Value }
        val userTokensContainsJupiter = userTokens
            .filter { jupiterMints.contains(it.mintAddress) }
        val maxUserTokens = userTokensContainsJupiter.maxByOrNull { it.total }

        val tokenA: SwapTokenModel
        val tokenB: SwapTokenModel

        when {
            haveUCDC -> {
                tokenA = SwapTokenModel.UserToken(userTokens.first { it.isUSDC })
                val userSol = userTokens.firstOrNull { it.isSOL }
                tokenB = if (userSol != null) {
                    SwapTokenModel.UserToken(userSol)
                } else {
                    val jupiterSol = jupiterTokens.first { it.tokenMint.base58Value == Constants.WRAPPED_SOL_MINT }
                    SwapTokenModel.JupiterToken(jupiterSol)
                }
            }
            haveSOL -> {
                tokenA = SwapTokenModel.UserToken(userTokens.first { it.isSOL })
                val jupiterUSDC = jupiterTokens.first { it.tokenSymbol == Constants.USDC_SYMBOL }
                tokenB = SwapTokenModel.JupiterToken(jupiterUSDC)
            }
            maxUserTokens != null -> {
                tokenA = SwapTokenModel.UserToken(maxUserTokens)
                val jupiterSol = jupiterTokens.first { it.tokenMint.base58Value == Constants.WRAPPED_SOL_MINT }
                tokenB = SwapTokenModel.JupiterToken(jupiterSol)
            }
            else -> {
                val jupiterUSDC = jupiterTokens.first { it.tokenSymbol == Constants.USDC_SYMBOL }
                val jupiterSol = jupiterTokens.first { it.tokenMint.base58Value == Constants.WRAPPED_SOL_MINT }
                tokenA = SwapTokenModel.JupiterToken(jupiterUSDC)
                tokenB = SwapTokenModel.JupiterToken(jupiterSol)
            }
        }
        return@withContext tokenA to tokenB
    }
}
