package com.p2p.wallet.dashboard.model.local

data class ConstWallet(
    val tokenSymbol: String,
    val mint: String,
    val tokenName: String,
    val icon: String
) {

    fun isUS(): Boolean = tokenSymbol == "USDT" || tokenSymbol == "USDC"
}