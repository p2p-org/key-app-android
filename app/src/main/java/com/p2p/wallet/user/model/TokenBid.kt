package com.p2p.wallet.user.model

import java.math.BigDecimal

data class TokenBid(
    val symbol: String,
    val bid: BigDecimal
)