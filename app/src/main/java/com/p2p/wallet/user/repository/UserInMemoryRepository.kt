package com.p2p.wallet.user.repository

import com.p2p.wallet.main.model.TokenPrice
import com.p2p.wallet.user.model.TokenData
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.math.BigDecimal

class UserInMemoryRepository : UserLocalRepository {

    private val pricesFlow = MutableStateFlow<List<TokenPrice>>(emptyList())

    override fun setTokenPrices(prices: List<TokenPrice>) {
        pricesFlow.value = prices
    }

    override fun getPriceByToken(symbol: String): TokenPrice {
        val price = pricesFlow.value.firstOrNull { it.tokenSymbol == symbol }
        return if (price != null) {
            price
        } else {
            Timber.w("No price found for token $symbol, continuing with 0")
            TokenPrice(symbol, BigDecimal.ZERO)
        }
    }

    private val decimalsFlow = MutableStateFlow<List<TokenData>>(emptyList())

    override fun setTokenData(data: List<TokenData>) {
        decimalsFlow.value = data
    }

    override fun findTokenData(mintAddress: String): TokenData? {
        val data = decimalsFlow.value.firstOrNull { it.mintAddress == mintAddress }
        if (data == null) {
            Timber.w("No data found for $mintAddress")
        }

        return data
    }
}