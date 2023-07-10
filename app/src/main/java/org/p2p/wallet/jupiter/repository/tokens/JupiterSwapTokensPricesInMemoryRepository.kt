package org.p2p.wallet.jupiter.repository.tokens

import org.p2p.wallet.home.model.TokenPrice
import org.p2p.core.crypto.Base58String

class JupiterSwapTokensPricesInMemoryRepository : JupiterSwapTokensPricesLocalRepository {
    private val tokenMintsToPrice = mutableMapOf<Base58String, TokenPrice>()

    override fun getPriceByMint(mintAddress: Base58String): TokenPrice? {
        return tokenMintsToPrice[mintAddress]
    }

    override fun requirePriceByMint(mintAddress: Base58String): TokenPrice {
        return tokenMintsToPrice.getValue(mintAddress)
    }

    override fun update(prices: Map<Base58String, TokenPrice>) {
        tokenMintsToPrice.putAll(prices)
    }

    override operator fun contains(it: Base58String): Boolean = it in tokenMintsToPrice
}
