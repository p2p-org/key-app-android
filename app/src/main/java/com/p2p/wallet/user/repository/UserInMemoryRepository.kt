package com.p2p.wallet.user.repository

import com.p2p.wallet.main.model.TokenPrice
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.math.BigDecimal

class UserInMemoryRepository : UserLocalRepository {

    private val pricesFlow = MutableStateFlow<List<TokenPrice>>(emptyList())

    override fun setTokenPrices(prices: List<TokenPrice>) {
        pricesFlow.value = prices
    }

    override fun getPriceByToken(token: String): TokenPrice {
        val price = pricesFlow.value.firstOrNull { it.tokenSymbol == token }
        return if (price != null) {
            price
        } else {
            Timber.w("No price found for token $token, continuing with 0")
            TokenPrice(token, 0.toDouble())
        }
    }
}