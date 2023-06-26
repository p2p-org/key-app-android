package org.p2p.token.service.repository

import org.p2p.token.service.model.TokenServiceMetadata
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

interface TokenServiceLocalRepository {
    fun setTokensPrice(networkChain: TokenServiceNetwork, prices: List<TokenServicePrice>)
    fun findTokenPriceByAddress(networkChain: TokenServiceNetwork, address: String): TokenServicePrice?

    fun setTokensMetadata(networkChain: TokenServiceNetwork, metadata: List<TokenServiceMetadata>)
    fun findTokenMetadataByAddress(networkChain: TokenServiceNetwork, address: String): TokenServiceMetadata?
}
