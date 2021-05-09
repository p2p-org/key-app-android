package com.p2p.wallet.main.api

import com.google.gson.annotations.SerializedName

data class TokenPriceResponse(
    @SerializedName("SOL")
    val SOL: PriceResponse?,
    @SerializedName("SRM")
    val SRM: PriceResponse?,
    @SerializedName("MSRM")
    val MSRM: PriceResponse?,
    @SerializedName("BTC")
    val BTC: PriceResponse?,
    @SerializedName("ETH")
    val ETH: PriceResponse?,
    @SerializedName("FTT")
    val FTT: PriceResponse?,
    @SerializedName("YFI")
    val YFI: PriceResponse?,
    @SerializedName("LINK")
    val LINK: PriceResponse?,
    @SerializedName("XRP")
    val XRP: PriceResponse?,
    @SerializedName("USDT")
    val USDT: PriceResponse?,
    @SerializedName("USDC")
    val USDC: PriceResponse?,
    @SerializedName("WUSDC")
    val WUSDC: PriceResponse?,
    @SerializedName("SUSHI")
    val SUSHI: PriceResponse?,
    @SerializedName("ALEPH")
    val ALEPH: PriceResponse?,
    @SerializedName("SXP")
    val SXP: PriceResponse?,
    @SerializedName("HGET")
    val HGET: PriceResponse?,
    @SerializedName("CREAM")
    val CREAM: PriceResponse?,
    @SerializedName("UBXT")
    val UBXT: PriceResponse?,
    @SerializedName("HNT")
    val HNT: PriceResponse?,
    @SerializedName("FRONT")
    val FRONT: PriceResponse?,
    @SerializedName("AKRO")
    val AKRO: PriceResponse?,
    @SerializedName("HXRO")
    val HXRO: PriceResponse?,
    @SerializedName("UNI")
    val UNI: PriceResponse?,
    @SerializedName("MATH")
    val MATH: PriceResponse?,
    @SerializedName("TOMO")
    val TOMO: PriceResponse?,
    @SerializedName("LUA")
    val LUA: PriceResponse?
)

data class PriceResponse(
    @SerializedName("USD")
    val value: Double
)