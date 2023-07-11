package org.p2p.wallet.user.interactor

import timber.log.Timber
import org.p2p.core.token.Token
import org.p2p.token.service.api.events.manager.TokenServiceEventPublisher
import org.p2p.token.service.model.TokenServiceNetwork

class UserTokensInteractor(private val tokenServiceInteractor: TokenServiceEventPublisher) {

    fun loadUserRates(userTokens: List<Token.Active>) {
        Timber.i("Loading user rates for ${userTokens.size}")
        val tokenAddresses = userTokens.map { it.mintAddress }
        tokenServiceInteractor.loadTokensPrice(
            networkChain = TokenServiceNetwork.SOLANA,
            addresses = tokenAddresses
        )
    }
}
