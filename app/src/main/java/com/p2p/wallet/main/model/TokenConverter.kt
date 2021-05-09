package com.p2p.wallet.main.model

import com.p2p.wallet.main.api.TokenPriceResponse
import java.math.BigDecimal

object TokenConverter {

    fun fromNetwork(tokenSymbol: String, response: TokenPriceResponse): TokenPrice = when (tokenSymbol) {
        "SOL" -> TokenPrice(tokenSymbol, BigDecimal(response.SOL?.value ?: 0.0))
        "BTC" -> TokenPrice(tokenSymbol, BigDecimal(response.BTC?.value ?: 0.0))
        "SRM" -> TokenPrice(tokenSymbol, BigDecimal(response.SRM?.value ?: 0.0))
        "MSRM" -> TokenPrice(tokenSymbol, BigDecimal(response.MSRM?.value ?: 0.0))
        "ETH" -> TokenPrice(tokenSymbol, BigDecimal(response.ETH?.value ?: 0.0))
        "FTT" -> TokenPrice(tokenSymbol, BigDecimal(response.FTT?.value ?: 0.0))
        "YFI" -> TokenPrice(tokenSymbol, BigDecimal(response.YFI?.value ?: 0.0))
        "LINK" -> TokenPrice(tokenSymbol, BigDecimal(response.LINK?.value ?: 0.0))
        "XRP" -> TokenPrice(tokenSymbol, BigDecimal(response.XRP?.value ?: 0.0))
        "USDT" -> TokenPrice(tokenSymbol, BigDecimal(response.USDT?.value ?: 0.0))
        "USDC" -> TokenPrice(tokenSymbol, BigDecimal(response.USDC?.value ?: 0.0))
        "WUSDC" -> TokenPrice(tokenSymbol, BigDecimal(response.WUSDC?.value ?: 0.0))
        "SUSHI" -> TokenPrice(tokenSymbol, BigDecimal(response.SUSHI?.value ?: 0.0))
        "ALEPH" -> TokenPrice(tokenSymbol, BigDecimal(response.ALEPH?.value ?: 0.0))
        "SXP" -> TokenPrice(tokenSymbol, BigDecimal(response.SXP?.value ?: 0.0))
        "HGET" -> TokenPrice(tokenSymbol, BigDecimal(response.HGET?.value ?: 0.0))
        "CREAM" -> TokenPrice(tokenSymbol, BigDecimal(response.CREAM?.value ?: 0.0))
        "UBXT" -> TokenPrice(tokenSymbol, BigDecimal(response.UBXT?.value ?: 0.0))
        "HNT" -> TokenPrice(tokenSymbol, BigDecimal(response.HNT?.value ?: 0.0))
        "FRONT" -> TokenPrice(tokenSymbol, BigDecimal(response.FRONT?.value ?: 0.0))
        "AKRO" -> TokenPrice(tokenSymbol, BigDecimal(response.AKRO?.value ?: 0.0))
        "HXRO" -> TokenPrice(tokenSymbol, BigDecimal(response.HXRO?.value ?: 0.0))
        "UNI" -> TokenPrice(tokenSymbol, BigDecimal(response.UNI?.value ?: 0.0))
        "MATH" -> TokenPrice(tokenSymbol, BigDecimal(response.MATH?.value ?: 0.0))
        "TOMO" -> TokenPrice(tokenSymbol, BigDecimal(response.TOMO?.value ?: 0.0))
        "LUA" -> TokenPrice(tokenSymbol, BigDecimal(response.LUA?.value ?: 0.0))
        else -> throw IllegalStateException("Unknown token symbol: $tokenSymbol")
    }
}