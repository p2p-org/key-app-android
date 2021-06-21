package com.p2p.wallet.user.repository

import com.p2p.wallet.main.model.TokenPrice
import com.p2p.wallet.user.model.TokenBid
import com.p2p.wallet.user.model.TokenData
import kotlinx.coroutines.flow.Flow
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
            TokenPrice(token, BigDecimal.ZERO)
        }
    }

    private val bidsFlow = MutableStateFlow<List<TokenBid>>(emptyList())

    override fun setTokenBids(bids: List<TokenBid>) {
        bidsFlow.value = bids
    }

    override fun getBidByToken(token: String): TokenBid {
        val price = bidsFlow.value.firstOrNull { it.symbol == token }
        return if (price != null) {
            price
        } else {
            Timber.w("No bid found for token $token, continuing with 0")
            TokenBid(token, BigDecimal.ZERO)
        }
    }

    private val decimalsFlow = MutableStateFlow<List<TokenData>>(emptyList())

    override fun setTokenData(decimals: List<TokenData>) {
        decimalsFlow.value = decimals
    }

    override fun getTokenData(mintAddress: String): TokenData? =
        decimalsFlow.value.firstOrNull { it.mintAddress == mintAddress }

    override fun getTokenDataFlow(): Flow<List<TokenData>> = decimalsFlow
}