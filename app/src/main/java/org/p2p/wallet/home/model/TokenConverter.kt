package org.p2p.wallet.home.model

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.token.TokenVisibility
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toPowerValue
import org.p2p.ethereumkit.external.api.response.TokenMetadataResponse
import org.p2p.solanaj.model.types.Account
import org.p2p.wallet.home.db.TokenEntity
import org.p2p.wallet.user.local.TokenResponse

object TokenConverter {

    fun fromNetwork(response: TokenResponse): TokenData =
        TokenData(
            mintAddress = response.address,
            name = response.name,
            symbol = response.symbol,
            iconUrl = response.logoUrl,
            decimals = response.decimals,
            isWrapped = response.isWrapped(),
            serumV3Usdc = response.extensions?.serumV3Usdc,
            serumV3Usdt = response.extensions?.serumV3Usdt,
            coingeckoId = response.extensions?.coingeckoId
        )

    fun fromNetwork(address: String, response: TokenMetadataResponse): TokenData =
        TokenData(
            mintAddress = address,
            name = response.tokenName.orEmpty(),
            symbol = response.symbol.orEmpty(),
            iconUrl = response.logoUrl,
            decimals = response.decimals,
            isWrapped = false,
            serumV3Usdc = null,
            serumV3Usdt = null,
            coingeckoId = null
        )

    fun fromNetwork(
        account: Account,
        tokenData: TokenData,
        price: TokenPrice?
    ): Token.Active {
        val data = account.account.data
        val mintAddress = data.parsed.info.mint
        val total = data.parsed.info.tokenAmount.amount.toBigInteger()
        return Token.Active(
            publicKey = account.pubkey,
            tokenSymbol = tokenData.symbol,
            decimals = tokenData.decimals,
            mintAddress = mintAddress,
            tokenName = tokenData.name,
            iconUrl = tokenData.iconUrl,
            totalInUsd = price?.let { total.fromLamports(tokenData.decimals).times(it.price).scaleMedium() },
            total = BigDecimal(total).divide(tokenData.decimals.toPowerValue()),
            rate = price?.price,
            visibility = TokenVisibility.DEFAULT,
            serumV3Usdc = tokenData.serumV3Usdc,
            serumV3Usdt = tokenData.serumV3Usdt,
            isWrapped = tokenData.isWrapped
        )
    }

    fun fromNetwork(
        data: TokenData,
        price: TokenPrice?
    ): Token.Other =
        Token.Other(
            tokenName = data.name,
            tokenSymbol = data.symbol,
            decimals = data.decimals,
            mintAddress = data.mintAddress,
            iconUrl = data.iconUrl,
            serumV3Usdc = data.serumV3Usdc,
            serumV3Usdt = data.serumV3Usdt,
            isWrapped = data.isWrapped,
            rate = price?.price
        )

    fun toDatabase(token: Token.Active): TokenEntity =
        TokenEntity(
            publicKey = token.publicKey,
            tokenSymbol = token.tokenSymbol,
            decimals = token.decimals,
            mintAddress = token.mintAddress,
            tokenName = token.tokenName,
            iconUrl = token.iconUrl,
            totalInUsd = token.totalInUsd,
            total = token.total,
            exchangeRate = token.rate?.toString(),
            visibility = token.visibility.stringValue,
            serumV3Usdc = token.serumV3Usdc,
            serumV3Usdt = token.serumV3Usdt,
            isWrapped = token.isWrapped
        )

    fun fromDatabase(entity: TokenEntity): Token.Active =
        Token.Active(
            publicKey = entity.publicKey,
            tokenSymbol = entity.tokenSymbol,
            decimals = entity.decimals,
            mintAddress = entity.mintAddress,
            tokenName = entity.tokenName,
            iconUrl = entity.iconUrl,
            totalInUsd = entity.totalInUsd,
            total = entity.total,
            rate = entity.exchangeRate?.toBigDecimalOrZero(),
            visibility = TokenVisibility.parse(entity.visibility),
            serumV3Usdc = entity.serumV3Usdc,
            serumV3Usdt = entity.serumV3Usdt,
            isWrapped = entity.isWrapped
        )

    fun toTokenData(token: Token): TokenData =
        TokenData(
            mintAddress = token.mintAddress,
            name = token.tokenName,
            symbol = token.tokenSymbol,
            iconUrl = token.iconUrl,
            decimals = token.decimals,
            isWrapped = token.isWrapped,
            serumV3Usdc = token.serumV3Usdc,
            serumV3Usdt = token.serumV3Usdt,
            coingeckoId = null
        )
}
