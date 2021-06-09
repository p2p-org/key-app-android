package com.p2p.wallet.user.model

data class TokenData(
    val mintAddress: String,
    val name: String,
    val symbol: String,
    val iconUrl: String?,
    val decimals: Int
)