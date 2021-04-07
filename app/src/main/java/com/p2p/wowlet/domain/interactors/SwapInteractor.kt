package com.p2p.wowlet.domain.interactors

interface SwapInteractor {

    fun getAroundToCurrencyValue(amount: String, walletBinds: Double, isInCryptoCurrency: Boolean): Double
    fun getAmountInConvertingToken(amount: String, from: Double, to: Double): Double
    fun getTokenPerToken(from: Double, to: Double): Double
}