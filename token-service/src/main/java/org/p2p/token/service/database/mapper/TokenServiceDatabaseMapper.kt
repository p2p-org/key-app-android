package org.p2p.token.service.database.mapper

import org.p2p.token.service.database.entity.TokenPriceEntity
import org.p2p.token.service.database.entity.TokenRateEntity
import org.p2p.token.service.model.TokenRate
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

internal class TokenServiceDatabaseMapper() {

    fun toEntity(item: TokenServicePrice): TokenPriceEntity? {
        val tokenRate = item.rate ?: return null
        val tokenRateEntity = toEntity(item.address, tokenRate) ?: return null
        return TokenPriceEntity(
            tokenAddress = item.address,
            networkChain = item.network.networkName,
            tokenRate = tokenRateEntity
        )
    }

    fun toEntity(address: String, tokenRate: TokenRate): TokenRateEntity? {
        return TokenRateEntity(
            tokenAddress = address,
            usd = tokenRate.usd
        )
    }

    fun fromEntity(entity: TokenPriceEntity): TokenServicePrice {
        return TokenServicePrice(
            address = entity.tokenAddress,
            rate = fromEntity(entity.tokenRate),
            network = TokenServiceNetwork.getValueOf(entity.networkChain)
        )
    }

    fun fromEntity(entity: TokenRateEntity): TokenRate {
        return TokenRate(entity.usd)
    }
}
