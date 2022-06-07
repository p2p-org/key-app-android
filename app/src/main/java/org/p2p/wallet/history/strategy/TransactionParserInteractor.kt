package org.p2p.wallet.history.strategy

import org.p2p.wallet.home.model.Token

class TransactionParserInteractor(
    private val userLocalRepository: UserLocalRepository
) {

    suspend fun getTokenWithMint(mint: String?): Token? {
        if(mint == null) return null
        val tokenData = userLocalRepository.findTokenData(mint)
    }
}
