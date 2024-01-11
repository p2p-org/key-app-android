package org.p2p.wallet.jupiter.statemanager.token_selector

import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.core.crypto.Base58String
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
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
        val userTokensJob = async { tokenServiceCoordinator.getUserTokens() }
        val userTokens: List<Token.Active> = userTokensJob.await()

        val jupiterUsdc = jupiterTokensRepository.requireUsdc()
        val jupiterSol = jupiterTokensRepository.requireWrappedSol()
        val defaultTokenA = SwapTokenModel.JupiterToken(jupiterUsdc)
        val defaultTokenB = SwapTokenModel.JupiterToken(jupiterSol)

        // search in user tokens first
        val tokenA = userTokens.findByTokenMint(preinstallTokenAMint)
            ?: jupiterTokensRepository.findTokenByMint(preinstallTokenAMint)?.let(SwapTokenModel::JupiterToken)
            ?: defaultTokenA

        val tokenB = userTokens.findByTokenMint(preinstallTokenBMint)
            ?: jupiterTokensRepository.findTokenByMint(preinstallTokenBMint)?.let(SwapTokenModel::JupiterToken)
            ?: defaultTokenB

        savedSelectedSwapTokenStorage.savedTokenAMint = tokenA.mintAddress
        savedSelectedSwapTokenStorage.savedTokenBMint = tokenB.mintAddress

        tokenA to tokenB
    }

    private fun List<Token.Active>.findByTokenMint(tokenMint: Base58String): SwapTokenModel.UserToken? {
        return firstOrNull { it.mintAddress == tokenMint.base58Value }
            ?.let(SwapTokenModel::UserToken)
    }
}
