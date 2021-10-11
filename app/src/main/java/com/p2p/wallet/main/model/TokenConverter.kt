package com.p2p.wallet.main.model

import com.p2p.wallet.main.api.TokenColors
import com.p2p.wallet.main.db.TokenEntity
import com.p2p.wallet.user.local.TokenResponse
import com.p2p.wallet.user.model.TokenData
import com.p2p.wallet.utils.fromLamports
import com.p2p.wallet.utils.toBigDecimalOrZero
import com.p2p.wallet.utils.toPowerValue
import org.p2p.solanaj.model.types.Account
import java.math.BigDecimal

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
            serumV3Usdt = response.extensions?.serumV3Usdt
        )

    fun fromNetwork(
        account: Account,
        tokenData: TokenData,
        price: TokenPrice
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
            logoUrl = tokenData.iconUrl,
            price = total.fromLamports(tokenData.decimals).times(price.price),
            total = BigDecimal(total).divide(tokenData.decimals.toPowerValue()),
            color = TokenColors.findColorBySymbol(tokenData.symbol),
            usdRate = price.price,
            visibility = TokenVisibility.DEFAULT,
            serumV3Usdc = tokenData.serumV3Usdc,
            serumV3Usdt = tokenData.serumV3Usdt,
            isWrapped = tokenData.isWrapped
        )
    }

    fun fromNetwork(
        data: TokenData
    ): Token.Inactive =
        Token.Inactive(
            tokenName = data.name,
            tokenSymbol = data.symbol,
            decimals = data.decimals,
            mintAddress = data.mintAddress,
            logoUrl = data.iconUrl,
            color = TokenColors.findColorBySymbol(data.symbol),
            serumV3Usdc = data.serumV3Usdc,
            serumV3Usdt = data.serumV3Usdt,
            isWrapped = data.isWrapped
        )

    fun toDatabase(token: Token.Active): TokenEntity =
        TokenEntity(
            tokenSymbol = token.tokenSymbol,
            publicKey = token.publicKey,
            decimals = token.decimals,
            mintAddress = token.mintAddress,
            tokenName = token.tokenName,
            iconUrl = token.logoUrl,
            price = token.price,
            total = token.total,
            color = token.color,
            exchangeRate = token.usdRate.toString(),
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
            logoUrl = entity.iconUrl,
            price = entity.price,
            total = entity.total,
            color = entity.color,
            usdRate = entity.exchangeRate.toBigDecimalOrZero(),
            visibility = TokenVisibility.parse(entity.visibility),
            serumV3Usdc = entity.serumV3Usdc,
            serumV3Usdt = entity.serumV3Usdt,
            isWrapped = entity.isWrapped
        )
}