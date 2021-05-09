package com.p2p.wallet.main.api

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class MultiPriceResponse(
    @SerializedName("SOL")
    val SOL: SinglePriceResponse?,
    @SerializedName("SRM")
    val SRM: SinglePriceResponse?,
    @SerializedName("MSRM")
    val MSRM: SinglePriceResponse?,
    @SerializedName("BTC")
    val BTC: SinglePriceResponse?,
    @SerializedName("ETH")
    val ETH: SinglePriceResponse?,
    @SerializedName("FTT")
    val FTT: SinglePriceResponse?,
    @SerializedName("YFI")
    val YFI: SinglePriceResponse?,
    @SerializedName("LINK")
    val LINK: SinglePriceResponse?,
    @SerializedName("XRP")
    val XRP: SinglePriceResponse?,
    @SerializedName("USDT")
    val USDT: SinglePriceResponse?,
    @SerializedName("USDC")
    val USDC: SinglePriceResponse?,
    @SerializedName("WUSDC")
    val WUSDC: SinglePriceResponse?,
    @SerializedName("SUSHI")
    val SUSHI: SinglePriceResponse?,
    @SerializedName("ALEPH")
    val ALEPH: SinglePriceResponse?,
    @SerializedName("SXP")
    val SXP: SinglePriceResponse?,
    @SerializedName("HGET")
    val HGET: SinglePriceResponse?,
    @SerializedName("CREAM")
    val CREAM: SinglePriceResponse?,
    @SerializedName("UBXT")
    val UBXT: SinglePriceResponse?,
    @SerializedName("HNT")
    val HNT: SinglePriceResponse?,
    @SerializedName("FRONT")
    val FRONT: SinglePriceResponse?,
    @SerializedName("AKRO")
    val AKRO: SinglePriceResponse?,
    @SerializedName("HXRO")
    val HXRO: SinglePriceResponse?,
    @SerializedName("UNI")
    val UNI: SinglePriceResponse?,
    @SerializedName("MATH")
    val MATH: SinglePriceResponse?,
    @SerializedName("TOMO")
    val TOMO: SinglePriceResponse?,
    @SerializedName("LUA")
    val LUA: SinglePriceResponse?
)

data class SinglePriceResponse(
    @SerializedName("USD")
    val value: Double
) {

    fun getValue(): BigDecimal = BigDecimal(value)
}