package org.p2p.wallet.jupiter.statemanager.token_selector

import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.core.utils.Constants
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapToken
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.tokenservice.TokenServiceCoordinator

class PreinstallTokensByMintSelector(
    private val jupiterTokensRepository: JupiterSwapTokensRepository,
    private val dispatchers: CoroutineDispatchers,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val savedSelectedSwapTokenStorage: JupiterSwapStorageContract,
    private val preinstallTokenAMint: Base58String,
    private val preinstallTokenBMint: Base58String,
) : SwapInitialTokenSelector {

    override suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel> = withContext(dispatchers.io) {
        val jupiterTokensJob = async { jupiterTokensRepository.getTokens() }
        val userTokensJob = async { tokenServiceCoordinator.getUserTokens() }
        val jupiterTokens: List<JupiterSwapToken> = jupiterTokensJob.await()
        val userTokens: List<Token.Active> = userTokensJob.await()

        val jupiterUsdc = jupiterTokens.first { it.tokenMint.base58Value == Constants.USDC_MINT }
        val jupiterSol = jupiterTokens.first { it.tokenMint.base58Value == Constants.WRAPPED_SOL_MINT }
        val defaultTokenA = SwapTokenModel.JupiterToken(jupiterUsdc)
        val defaultTokenB = SwapTokenModel.JupiterToken(jupiterSol)

        // search in user tokens first
        val tokenA = userTokens.findByTokenMint(preinstallTokenAMint)
            ?: jupiterTokens.findByTokenMint(preinstallTokenAMint)
            ?: defaultTokenA

        val tokenB = userTokens.findByTokenMint(preinstallTokenBMint)
            ?: jupiterTokens.findByTokenMint(preinstallTokenBMint)
            ?: defaultTokenB

        savedSelectedSwapTokenStorage.savedTokenAMint = tokenA.mintAddress
        savedSelectedSwapTokenStorage.savedTokenBMint = tokenB.mintAddress

        tokenA to tokenB
    }

    private fun List<JupiterSwapToken>.findByTokenMint(tokenMint: Base58String): SwapTokenModel.JupiterToken? {
        return firstOrNull { it.tokenMint == tokenMint }
            ?.let(SwapTokenModel::JupiterToken)
    }

    private fun List<Token.Active>.findByTokenMint(tokenMint: Base58String): SwapTokenModel.UserToken? {
        return firstOrNull { it.mintAddress == tokenMint.base58Value }
            ?.let(SwapTokenModel::UserToken)
    }
}
