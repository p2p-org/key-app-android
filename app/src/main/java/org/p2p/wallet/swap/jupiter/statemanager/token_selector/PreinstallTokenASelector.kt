package org.p2p.wallet.swap.jupiter.statemanager.token_selector

import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.p2p.core.token.Token
import org.p2p.wallet.home.repository.HomeLocalRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.swap.JupiterSelectedSwapTokenStorageContract
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.tokens.JupiterSwapTokensRepository

class PreinstallTokenASelector(
    private val jupiterTokensRepository: JupiterSwapTokensRepository,
    private val dispatchers: CoroutineDispatchers,
    private val homeLocalRepository: HomeLocalRepository,
    private val savedSelectedSwapTokenStorage: JupiterSelectedSwapTokenStorageContract,
    private val preinstallTokenA: Token.Active,
) : SwapInitialTokenSelector {

    override suspend fun getTokenPair(): Pair<SwapTokenModel, SwapTokenModel> = withContext(dispatchers.io) {
        val jupiterTokensJob = async { jupiterTokensRepository.getTokens() }
        val userTokensJob = async { homeLocalRepository.getUserTokens() }
        val jupiterTokens = jupiterTokensJob.await()
        val userTokens = userTokensJob.await()

        val tokenA: SwapTokenModel = SwapTokenModel.UserToken(preinstallTokenA)
        val tokenB: SwapTokenModel = getTokenB(
            jupiterTokens = jupiterTokens,
            userTokens = userTokens,
            findSolOrUsdc = !preinstallTokenA.isSOL,
            savedSwapTokenB = savedSelectedSwapTokenStorage.savedTokenBMint
        )
        savedSelectedSwapTokenStorage.savedTokenAMint = tokenA.mintAddress
        savedSelectedSwapTokenStorage.savedTokenBMint = tokenB.mintAddress

        tokenA to tokenB
    }
}
