package org.p2p.wallet.jupiter.repository.tokens

import org.p2p.wallet.home.model.TokenPrice
import org.p2p.core.crypto.Base58String

interface JupiterSwapTokensPricesLocalRepository {
    fun getPriceByMint(mintAddress: Base58String): TokenPrice?
    fun requirePriceByMint(mintAddress: Base58String): TokenPrice

    fun update(prices: Map<Base58String, TokenPrice>)
    operator fun contains(it: Base58String): Boolean
}
