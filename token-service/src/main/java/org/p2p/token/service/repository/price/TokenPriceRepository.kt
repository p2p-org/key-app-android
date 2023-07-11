package org.p2p.token.service.repository.price

import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.model.TokenServiceQueryResult

interface TokenPriceRepository {
    suspend fun loadTokensPrice(
        chain: TokenServiceNetwork,
        addresses: List<String>
    ): List<TokenServiceQueryResult<TokenServicePrice>>

}
