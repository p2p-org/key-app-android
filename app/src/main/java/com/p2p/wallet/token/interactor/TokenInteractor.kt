package com.p2p.wallet.token.interactor

import com.p2p.wallet.token.model.PriceHistory
import com.p2p.wallet.token.repository.TokenRepository

class TokenInteractor(
    private val tokenRepository: TokenRepository
) {

    suspend fun getDailyPriceHistory(sourceToken: String, destination: String, days: Int): List<PriceHistory> =
        tokenRepository.getDailyPriceHistory(sourceToken, destination, days)

    suspend fun getHourlyPriceHistory(sourceToken: String, destination: String, hours: Int): List<PriceHistory> =
        tokenRepository.getHourlyPriceHistory(sourceToken, destination, hours)
}