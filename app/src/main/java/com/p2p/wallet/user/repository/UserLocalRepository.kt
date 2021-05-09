package com.p2p.wallet.user.repository

import com.p2p.wallet.main.model.TokenPrice

interface UserLocalRepository {
    fun setTokenPrices(prices: List<TokenPrice>)
    fun getPriceByToken(token: String): TokenPrice
}