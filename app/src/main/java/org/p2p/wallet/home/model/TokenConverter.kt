package org.p2p.wallet.home.model

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.token.TokenData
import org.p2p.core.token.TokenVisibility
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toPowerValue
import org.p2p.solanaj.model.types.Account
import org.p2p.token.service.model.TokenServicePrice
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

    fun fromNetwork(
        mintAddress: String,
        totalLamports: BigInteger,
        accountPublicKey: String,
        tokenData: TokenData,
        price: TokenServicePrice?
    ): Token.Active {
        val totalInUsd = price?.let {
            totalLamports.fromLamports(tokenData.decimals).times(it.price)
        }
        val total = totalLamports.toBigDecimal().divide(tokenData.decimals.toPowerValue())
        return Token.Active(
            publicKey = accountPublicKey,
            tokenSymbol = tokenData.symbol,
            decimals = tokenData.decimals,
            mintAddress = mintAddress,
            tokenName = tokenData.name,
            iconUrl = tokenData.iconUrl,
            coingeckoId = tokenData.coingeckoId,
            totalInUsd = totalInUsd,
            total = total,
            rate = price?.price,
            visibility = TokenVisibility.DEFAULT,
            serumV3Usdc = tokenData.serumV3Usdc,
            serumV3Usdt = tokenData.serumV3Usdt,
            isWrapped = tokenData.isWrapped
        )
    }

    fun fromNetwork(
        account: Account,
        tokenData: TokenData,
        price: TokenServicePrice?
    ): Token.Active {
        val data = account.account.data.parsed.info
        val mintAddress = data.mint
        val total = data.tokenAmount.amount.toBigInteger()
        return fromNetwork(
            mintAddress = mintAddress,
            totalLamports = total,
            accountPublicKey = account.pubkey,
            tokenData = tokenData,
            price = price
        )
    }

    fun fromNetwork(
        data: TokenData,
        price: TokenServicePrice?
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
            coingeckoId = token.coingeckoId,
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
            coingeckoId = entity.coingeckoId,
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
