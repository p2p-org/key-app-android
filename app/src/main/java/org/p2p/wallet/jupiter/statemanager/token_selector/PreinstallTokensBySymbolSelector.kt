package org.p2p.wallet.jupiter.statemanager.token_selector

import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.core.utils.Constants
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository

class PreinstallTokensBySymbolSelector(
    private val jupiterTokensRepository: JupiterSwapTokensRepository,
    private val dispatchers: CoroutineDispatchers,
    private val homeLocalRepository: HomeLocalRepository,
    private val savedSelectedSwapTokenStorage: JupiterSwapStorageContract,
    private val preinstallTokenA: String,
    private val preinstallTokenB: String,
) : SwapInitialTokenSelector {

    override suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel> = withContext(dispatchers.io) {
        val jupiterTokensJob = async { jupiterTokensRepository.getTokens() }
        val userTokensJob = async { homeLocalRepository.getUserTokens() }
        val jupiterTokens = jupiterTokensJob.await()
        val userTokens = userTokensJob.await()

        val jupiterUsdc = jupiterTokens.first { it.tokenSymbol == Constants.USDC_SYMBOL }
        val jupiterSol = jupiterTokens.first { it.tokenMint.base58Value == Constants.WRAPPED_SOL_MINT }
        val defaultTokenA = SwapTokenModel.JupiterToken(jupiterUsdc)
        val defaultTokenB = SwapTokenModel.JupiterToken(jupiterSol)

        val tokenA = jupiterTokens
            // find jupiter token
            .firstOrNull { it.tokenSymbol.lowercase() == preinstallTokenA.lowercase() }
            ?.let(SwapTokenModel::JupiterToken)
            // if not found, find user token
            ?: userTokens
                .firstOrNull { it.tokenSymbol.lowercase() == preinstallTokenA.lowercase() }
                ?.let(SwapTokenModel::UserToken)
            // if not found, use default token
            ?: defaultTokenA

        val tokenB = jupiterTokens
            // find jupiter token
            .firstOrNull { it.tokenSymbol.lowercase() == preinstallTokenB.lowercase() }
            ?.let(SwapTokenModel::JupiterToken)
            // if not found, find user token
            ?: userTokens
                .firstOrNull { it.tokenSymbol.lowercase() == preinstallTokenB.lowercase() }
                ?.let(SwapTokenModel::UserToken)
            // if not found, use default token
            ?: defaultTokenB

        savedSelectedSwapTokenStorage.savedTokenAMint = tokenA.mintAddress
        savedSelectedSwapTokenStorage.savedTokenBMint = tokenB.mintAddress

        tokenA to tokenB
    }
}
