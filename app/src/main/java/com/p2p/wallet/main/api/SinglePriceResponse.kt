package com.p2p.wallet.main.api

import com.google.gson.annotations.SerializedName

data class SinglePriceResponse(
    @SerializedName("USD")
    val usdValue: Double?,
    @SerializedName("SOL")
    val SOL: Double?,
    @SerializedName("SRM")
    val SRM: Double?,
    @SerializedName("MSRM")
    val MSRM: Double?,
    @SerializedName("BTC")
    val BTC: Double?,
    @SerializedName("ETH")
    val ETH: Double?,
    @SerializedName("FTT")
    val FTT: Double?,
    @SerializedName("YFI")
    val YFI: Double?,
    @SerializedName("LINK")
    val LINK: Double?,
    @SerializedName("XRP")
    val XRP: Double?,
    @SerializedName("USDT")
    val USDT: Double?,
    @SerializedName("USDC")
    val USDC: Double?,
    @SerializedName("WUSDC")
    val WUSDC: Double?,
    @SerializedName("SUSHI")
    val SUSHI: Double?,
    @SerializedName("ALEPH")
    val ALEPH: Double?,
    @SerializedName("SXP")
    val SXP: Double?,
    @SerializedName("HGET")
    val HGET: Double?,
    @SerializedName("CREAM")
    val CREAM: Double?,
    @SerializedName("UBXT")
    val UBXT: Double?,
    @SerializedName("HNT")
    val HNT: Double?,
    @SerializedName("FRONT")
    val FRONT: Double?,
    @SerializedName("AKRO")
    val AKRO: Double?,
    @SerializedName("HXRO")
    val HXRO: Double?,
    @SerializedName("UNI")
    val UNI: Double?,
    @SerializedName("MATH")
    val MATH: Double?,
    @SerializedName("TOMO")
    val TOMO: Double?,
    @SerializedName("LUA")
    val LUA: Double?
)