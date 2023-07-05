package org.p2p.token.service.database.mapper

import org.p2p.token.service.database.entity.TokenPriceEntity
import org.p2p.token.service.model.TokenRate
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

internal class TokenServiceDatabaseMapper() {

    fun toEntity(item: TokenServicePrice): TokenPriceEntity? {
        val tokenRate = item.rate
        val usdRate = tokenRate.usd ?: return null
        return TokenPriceEntity(
            tokenAddress = item.address,
            networkChain = item.network.networkName,
            usdRate = usdRate
        )
    }

    fun fromEntity(entity: TokenPriceEntity): TokenServicePrice {
        return TokenServicePrice(
            address = entity.tokenAddress,
            rate = TokenRate(
                usd = entity.usdRate
            ),
            network = TokenServiceNetwork.getValueOf(entity.networkChain)
        )
    }
}
