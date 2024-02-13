package org.p2p.token.service.database.mapper

import org.p2p.token.service.database.entity.TokenServicePriceEntity
import org.p2p.token.service.model.TokenRate
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

internal class TokenServiceDatabaseMapper {

    fun toEntity(item: TokenServicePrice): TokenServicePriceEntity? {
        val tokenRate = item.rate
        val usdRate = tokenRate.usd ?: return null
        return TokenServicePriceEntity(
            tokenAddress = item.tokenAddress,
            networkChainName = item.network.networkName,
            usdRate = usdRate
        )
    }

    fun fromEntity(entity: TokenServicePriceEntity): TokenServicePrice {
        return TokenServicePrice(
            tokenAddress = entity.tokenAddress,
            rate = TokenRate(
                usd = entity.usdRate
            ),
            network = TokenServiceNetwork.getValueOf(entity.networkChainName)
        )
    }
}
