package org.p2p.wallet.user.interactor

import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.token.Token
import org.p2p.solanaj.core.PublicKey
import org.p2p.token.service.api.events.manager.TokenServiceEventPublisher
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.home.model.TokenComparator
import org.p2p.wallet.user.repository.UserTokensLocalRepository
import org.p2p.wallet.user.repository.UserTokensRepository

class UserTokensInteractor(
    private val tokenServiceInteractor: TokenServiceEventPublisher,
    private val userTokensLocalRepository: UserTokensLocalRepository,
    private val userTokensRepository: UserTokensRepository,
    private val dispatchers: CoroutineDispatchers
) {

    suspend fun loadUserRates(userTokens: List<Token.Active>) {
        val tokenAddresses = userTokens.map { it.mintAddress }
        tokenServiceInteractor.loadTokensPrice(
            networkChain = TokenServiceNetwork.SOLANA,
            addresses = tokenAddresses
        )
    }

    suspend fun loadUserTokens(publicKey: PublicKey): List<Token.Active> {
        return userTokensRepository.loadUserTokens(publicKey)
    }

    suspend fun saveUserTokens(tokens: List<Token.Active>) = withContext(dispatchers.io) {
        val cachedTokens = userTokensLocalRepository.getUserTokens()
        tokens.map { newToken ->
            val oldTokens = cachedTokens.find { oldTokens -> oldTokens.publicKey == newToken.publicKey }
            oldTokens?.visibility?.let { newToken.copy(visibility = it) }
            newToken
        }
            .sortedWith(TokenComparator())
            .let { userTokensLocalRepository.updateTokens(it) }
    }

    suspend fun saveUserTokensRates(tokensRates: List<TokenServicePrice>) = withContext(dispatchers.io) {
        userTokensLocalRepository.saveRatesForTokens(tokensRates)
    }
}
