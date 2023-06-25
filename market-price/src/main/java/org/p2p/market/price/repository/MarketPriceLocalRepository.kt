package org.p2p.market.price.repository

import timber.log.Timber
import java.math.BigDecimal
import org.p2p.market.price.api.response.NetworkChain

private const val TAG = "MarketPriceLocalRepository"
internal class MarketPriceLocalRepository {
    private val tokenMarketPriceCache = mutableMapOf<NetworkChain,Map<String,BigDecimal>>()

    fun saveMarketPrice(networkChain: NetworkChain, prices: Map<String,BigDecimal>) {
        Timber.tag(TAG).d("New items cached\nchain = $networkChain\n items = ${prices.size}")
        tokenMarketPriceCache[networkChain] = prices
    }

    fun findMarketPriceByAddress(networkChain: NetworkChain,address: String): BigDecimal?  {
        val networkMarketPrices = tokenMarketPriceCache[networkChain]
        return networkMarketPrices?.get(address)
    }
}
