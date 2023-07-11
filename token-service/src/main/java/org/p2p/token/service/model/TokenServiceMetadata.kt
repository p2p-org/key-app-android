package org.p2p.token.service.model

data class TokenServiceMetadata(
    val address: String,
    val symbol: String,
    val logoUrl: String,
    val decimals: Int,
    val price: TokenRate
)
