package org.p2p.wallet.user.repository

import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.main.model.TokenPrice
import org.p2p.wallet.user.model.TokenData
import timber.log.Timber

class UserInMemoryRepository : UserLocalRepository {

    private val pricesFlow = MutableStateFlow<List<TokenPrice>>(emptyList())

    override fun setTokenPrices(prices: List<TokenPrice>) {
        pricesFlow.value = prices
    }

    override fun getPriceByToken(symbol: String): TokenPrice? =
        pricesFlow.value.firstOrNull { it.tokenSymbol == symbol }

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

    override fun findTokenDataBySymbol(symbol: String): TokenData? {
        val data = decimalsFlow.value.firstOrNull { it.mintAddress == symbol }
        if (data == null) {
            Timber.w("No data found for $symbol")
        }

        return data
    }
}