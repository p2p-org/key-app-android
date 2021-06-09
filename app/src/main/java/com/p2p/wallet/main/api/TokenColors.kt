package com.p2p.wallet.main.api

import com.p2p.wallet.R
import com.p2p.wallet.main.model.TokenColor

object TokenColors {

    private val data = tokensColors()

    fun findColorBySymbol(symbol: String): Int {
        val token = data.firstOrNull { it.symbol == symbol }
        return token?.color ?: R.color.chartSOL
    }

    fun getSymbols() = data.map { it.symbol }

    private fun tokensColors() = mutableListOf(
        TokenColor(
            "SOL",
            R.color.chartSOL
        ),
        TokenColor(
            "MSRM",
            R.color.chartSRM
        ),
        TokenColor(
            "SRM",
            R.color.chartSRM
        ),
        TokenColor(
            "BTC",
            R.color.chartBTC
        ),
        TokenColor(
            "ETH",
            R.color.chartETH
        ),
        TokenColor(
            "FTT",
            R.color.chartFTX
        ),
        TokenColor(
            "YFI",
            R.color.chartYFI
        ),
        TokenColor(
            "LINK",
            R.color.chartLINK
        ),
        TokenColor(
            "XRP",
            R.color.chartXRP
        ),
        TokenColor(
            "USDT",
            R.color.chartUSDT
        ),
        TokenColor(
            "USDC",
            R.color.chartUSDC
        ),
        TokenColor(
            "WUSDC",
            R.color.chartUSDC
        ),
        TokenColor(
            "SUSHI",
            R.color.chartSUSHI
        ),
        TokenColor(
            "ALEPH",
            R.color.chartALEPH
        ),
        TokenColor(
            "SXP",
            R.color.chartSXP
        ),
        TokenColor(
            "HGET",
            R.color.chartHGET
        ),
        TokenColor(
            "CREAM",
            R.color.chartCREAM
        ),
        TokenColor(
            "UBXT",
            R.color.chartUBXT
        ),
        TokenColor(
            "HNT",
            R.color.chartHNT
        ),
        TokenColor(
            "FRONT",
            R.color.chartFRONT
        ),
        TokenColor(
            "AKRO",
            R.color.chartAKRO
        ),
        TokenColor(
            "HXRO",
            R.color.chartHXRO
        ),
        TokenColor(
            "UNI",
            R.color.chartUNI
        ),
        TokenColor(
            "MATH",
            R.color.chartMATH
        ),
        TokenColor(
            "TOMO",
            R.color.chartTOMO
        ),
        TokenColor(
            "LUA",
            R.color.chartLUA
        ),
    )
}