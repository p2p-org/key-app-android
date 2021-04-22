package com.p2p.wallet.dashboard.model.local

import androidx.annotation.ColorRes

data class ConstWallet(
    val tokenSymbol: String,
    val mint: String,
    val tokenName: String,
    val icon: String,
    @ColorRes val color: Int
) {

    fun isUS(): Boolean = tokenSymbol == "USDT" || tokenSymbol == "USDC"
}