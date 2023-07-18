package org.p2p.wallet.home.model

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.token.TokenMetadata
import org.p2p.core.token.TokenExtension
import org.p2p.core.token.TokenVisibility
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toPowerValue
import org.p2p.solanaj.model.types.Account
import org.p2p.token.service.api.response.TokenExtensionResponse
import org.p2p.token.service.api.response.TokenResponse
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.home.db.TokenEntity
import org.p2p.wallet.home.db.TokenExtensionEntity

object TokenConverter {

    fun fromNetwork(response: TokenResponse): TokenMetadata =
        TokenMetadata(
            mintAddress = response.address,
            name = response.name,
            symbol = response.symbol,
            iconUrl = response.logoUrl,
            decimals = response.decimals,
            isWrapped = response.isWrapped(),
            extensions = fromNetwork(response.extensions)
        )

    private fun fromNetwork(response: TokenExtensionResponse?): TokenExtension {
        return TokenExtension(
            ruleOfProcessingTokenPriceWs = response?.ruleOfProcessingTokenPriceWs,
            isPositionOnWs = response?.isPositionOnWs,
            isTokenCellVisibleOnWs = response?.isTokenCellVisibleOnWs,
            percentDifferenceToShowByPriceOnWs = response?.percentDifferenceToShowByPriceOnWs,
            calculationOfFinalBalanceOnWs = response?.calculationOfFinalBalanceOnWs,
            ruleOfFractionalPartOnWs = response?.ruleOfFractionalPartOnWs,
            canBeHidden = response?.canBeHidden
        )
    }

    fun fromNetwork(
        mintAddress: String,
        totalLamports: BigInteger,
        accountPublicKey: String,
        tokenMetadata: TokenMetadata,
        price: TokenServicePrice?
    ): Token.Active {
        val tokenRate = price?.usdRate
        val totalInUsd = if (tokenRate != null) {
            totalLamports.fromLamports(tokenMetadata.decimals).times(tokenRate)
        } else {
            null
        }
        val total = totalLamports.toBigDecimal().divide(tokenMetadata.decimals.toPowerValue())
        return Token.Active(
            publicKey = accountPublicKey,
            tokenSymbol = tokenMetadata.symbol,
            decimals = tokenMetadata.decimals,
            mintAddress = mintAddress,
            tokenName = tokenMetadata.name,
            iconUrl = tokenMetadata.iconUrl,
            totalInUsd = totalInUsd,
            total = total,
            rate = tokenRate,
            visibility = TokenVisibility.DEFAULT,
            isWrapped = tokenMetadata.isWrapped,
            extensions = tokenMetadata.extensions
        )
    }

    fun fromNetwork(
        account: Account,
        tokenMetadata: TokenMetadata,
        price: TokenServicePrice?
    ): Token.Active {
        val data = account.account.data.parsed.info
        val mintAddress = data.mint
        val total = data.tokenAmount.amount.toBigInteger()
        return fromNetwork(
            mintAddress = mintAddress,
            totalLamports = total,
            accountPublicKey = account.pubkey,
            tokenMetadata = tokenMetadata,
            price = price
        )
    }

    fun fromNetwork(
        data: TokenMetadata,
        price: TokenServicePrice?
    ): Token.Other =
        Token.Other(
            tokenName = data.name,
            tokenSymbol = data.symbol,
            decimals = data.decimals,
            mintAddress = data.mintAddress,
            iconUrl = data.iconUrl,
            isWrapped = data.isWrapped,
            rate = price?.rate?.usd
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
            isWrapped = token.isWrapped,
            extensions = toDatabase(token.extensions)
        )

    fun toDatabase(tokenExtension: TokenExtension?): TokenExtensionEntity {
        return TokenExtensionEntity(
            ruleOfProcessingTokenPriceWs = tokenExtension?.ruleOfProcessingTokenPriceWs,
            isPositionOnWs = tokenExtension?.isPositionOnWs,
            isTokenCellVisibleOnWs = tokenExtension?.isTokenCellVisibleOnWs,
            percentDifferenceToShowByPriceOnWs = tokenExtension?.percentDifferenceToShowByPriceOnWs,
            calculationOfFinalBalanceOnWs = tokenExtension?.calculationOfFinalBalanceOnWs,
            ruleOfFractionalPartOnWs = tokenExtension?.ruleOfFractionalPartOnWs,
            canBeHidden = tokenExtension?.canBeHidden
        )
    }

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
            isWrapped = entity.isWrapped,
            extensions = fromDatabase(entity.extensions)
        )

    fun fromDatabase(entity: TokenExtensionEntity?): TokenExtension =
        TokenExtension(
            ruleOfProcessingTokenPriceWs = entity?.ruleOfProcessingTokenPriceWs,
            isPositionOnWs = entity?.isPositionOnWs,
            isTokenCellVisibleOnWs = entity?.isTokenCellVisibleOnWs,
            percentDifferenceToShowByPriceOnWs = entity?.percentDifferenceToShowByPriceOnWs,
            calculationOfFinalBalanceOnWs = entity?.calculationOfFinalBalanceOnWs,
            ruleOfFractionalPartOnWs = entity?.ruleOfFractionalPartOnWs,
            canBeHidden = entity?.canBeHidden
        )
}
