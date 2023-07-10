package org.p2p.token.service.model

data class TokenServiceQueryResult<T>(
    val networkChain: TokenServiceNetwork,
    val items: List<T>
)
