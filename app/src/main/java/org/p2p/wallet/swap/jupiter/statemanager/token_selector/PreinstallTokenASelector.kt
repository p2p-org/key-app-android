package org.p2p.wallet.swap.jupiter.statemanager.token_selector

import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class PreinstallTokenASelector(
    private val jupiterTokensRepository: JupiterSwapTokensRepository,
    private val homeLocalRepository: HomeLocalRepository,
    private val dispatchers: CoroutineDispatchers,
    private val preinstallTokenA: Token.Active,
) : InitialTokenSelector {

    override suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel> = withContext(dispatchers.io) {
        val jupiterTokensJob = async { jupiterTokensRepository.getTokens() }
        val jupiterTokens = jupiterTokensJob.await()

        val tokenA: SwapTokenModel = SwapTokenModel.UserToken(preinstallTokenA)

        val tokenB: SwapTokenModel = when {
            preinstallTokenA.isSOL -> {
                val jupiterUSDC = jupiterTokens.first { it.tokenSymbol == Constants.USDC_SYMBOL }
                SwapTokenModel.JupiterToken(jupiterUSDC)
            }
            else -> {
                val jupiterSol = jupiterTokens.first { it.tokenMint.base58Value == Constants.WRAPPED_SOL_MINT }
                SwapTokenModel.JupiterToken(jupiterSol)
            }
        }
        return@withContext tokenA to tokenB
    }
}