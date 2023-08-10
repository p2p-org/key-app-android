package org.p2p.wallet.jupiter.statemanager.token_selector

import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.tokenservice.TokenServiceCoordinator

// not good, better to preinstall tokens by MINT, not symbols
// todo: change selector to select by mint
class PreinstallTokensBySymbolSelector(
    private val jupiterTokensRepository: JupiterSwapTokensRepository,
    private val dispatchers: CoroutineDispatchers,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val savedSelectedSwapTokenStorage: JupiterSwapStorageContract,
    private val preinstallTokenASymbol: String,
    private val preinstallTokenBSymbol: String,
) : SwapInitialTokenSelector {

    override suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel> = withContext(dispatchers.io) {
        val jupiterTokensJob = async { jupiterTokensRepository.getTokens() }
        val userTokensJob = async { tokenServiceCoordinator.getUserTokens() }
        val jupiterTokens: List<JupiterSwapToken> = jupiterTokensJob.await()
        val userTokens: List<Token.Active> = userTokensJob.await()

        val jupiterUsdc = jupiterTokens.first { it.tokenSymbol == Constants.USDC_SYMBOL }
        val jupiterSol = jupiterTokens.first { it.tokenMint.base58Value == Constants.WRAPPED_SOL_MINT }
        val defaultTokenA = SwapTokenModel.JupiterToken(jupiterUsdc)
        val defaultTokenB = SwapTokenModel.JupiterToken(jupiterSol)

        // search in user tokens first
        val tokenA = userTokens.findByTokenSymbol(preinstallTokenASymbol)
            ?: jupiterTokens.findByTokenSymbol(preinstallTokenASymbol)
            ?: defaultTokenA

        val tokenB = userTokens.findByTokenSymbol(preinstallTokenBSymbol)
            ?: jupiterTokens.findByTokenSymbol(preinstallTokenBSymbol)
            ?: defaultTokenB

        savedSelectedSwapTokenStorage.savedTokenAMint = tokenA.mintAddress
        savedSelectedSwapTokenStorage.savedTokenBMint = tokenB.mintAddress

        tokenA to tokenB
    }

    private fun List<JupiterSwapToken>.findByTokenSymbol(tokenSymbol: String): SwapTokenModel.JupiterToken? {
        return firstOrNull { it.tokenSymbol.lowercase() == tokenSymbol.lowercase() }
            ?.let(SwapTokenModel::JupiterToken)
    }

    private fun List<Token.Active>.findByTokenSymbol(tokenSymbol: String): SwapTokenModel.UserToken? {
        return firstOrNull { it.tokenSymbol.lowercase() == tokenSymbol.lowercase() }
            ?.let(SwapTokenModel::UserToken)
    }
}
