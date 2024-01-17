package org.p2p.wallet.jupiter.statemanager.token_selector

import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.wallet.infrastructure.swap.JupiterSwapStorageContract
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.tokens.JupiterSwapTokensRepository
import org.p2p.wallet.tokenservice.TokenServiceCoordinator

class PreinstallTokenASelector(
    private val jupiterTokensRepository: JupiterSwapTokensRepository,
    private val dispatchers: CoroutineDispatchers,
    private val tokenServiceCoordinator: TokenServiceCoordinator,
    private val savedSelectedSwapTokenStorage: JupiterSwapStorageContract,
    private val preinstallTokenA: Token.Active,
) : SwapInitialTokenSelector {

    override suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel> = withContext(dispatchers.io) {
        val userTokensJob = async { tokenServiceCoordinator.getUserTokens() }
        val userTokens = userTokensJob.await()

        val tokenA: SwapTokenModel = SwapTokenModel.UserToken(preinstallTokenA)
        val tokenB: SwapTokenModel = getTokenB(
            jupiterTokensRepository = jupiterTokensRepository,
            userTokens = userTokens,
            preferSol = !preinstallTokenA.isSOL,
            savedSwapTokenB = savedSelectedSwapTokenStorage.savedTokenBMint
        )
        savedSelectedSwapTokenStorage.savedTokenAMint = tokenA.mintAddress
        savedSelectedSwapTokenStorage.savedTokenBMint = tokenB.mintAddress

        tokenA to tokenB
    }
}
