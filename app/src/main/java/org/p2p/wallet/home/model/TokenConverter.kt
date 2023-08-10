package org.p2p.wallet.home.model

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.token.TokenExtensions
import org.p2p.core.token.TokenMetadata
import org.p2p.core.token.TokenVisibility
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toPowerValue
import org.p2p.solanaj.model.types.Account
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.token.service.repository.configurator.TokenExtensionsConfigurator
import org.p2p.wallet.home.db.TokenEntity
import org.p2p.wallet.home.db.TokenExtensionEntity

// TODO: We should transfer this object to class since for unit tests
object TokenConverter {

    fun createToken(
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

        val extensions = TokenExtensionsConfigurator(
            extensions = tokenMetadata.extensions,
            tokenTotal = total,
            tokenRate = totalInUsd
        ).config()

        return Token.Active(
            publicKey = accountPublicKey,
            tokenSymbol = tokenMetadata.symbol,
            decimals = tokenMetadata.decimals,
            mintAddress = mintAddress,
            tokenName = tokenMetadata.name,
            iconUrl = tokenMetadata.iconUrl,
            // TODO: Remove [totalInUsd] from constructor fields and make it as a function by calculating total * rate
            totalInUsd = if (extensions.isRateExceedsTheDifference) totalInUsd else total,
            total = total,
            rate = tokenRate,
            visibility = TokenVisibility.DEFAULT,
            isWrapped = tokenMetadata.isWrapped,
            tokenServiceAddress = tokenMetadata.mintAddress,
            tokenExtensions = extensions
        )
    }

    fun fromNetwork(
        tokenMetadata: TokenMetadata,
        price: TokenServicePrice?
    ): Token.Other {
        val extensions = TokenExtensionsConfigurator(
            extensions = tokenMetadata.extensions
        ).config()

        return Token.Other(
            tokenName = tokenMetadata.name,
            tokenSymbol = tokenMetadata.symbol,
            decimals = tokenMetadata.decimals,
            mintAddress = tokenMetadata.mintAddress,
            iconUrl = tokenMetadata.iconUrl,
            isWrapped = tokenMetadata.isWrapped,
            rate = price?.rate?.usd,
            tokenExtensions = extensions
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
        return createToken(
            mintAddress = mintAddress,
            totalLamports = total,
            accountPublicKey = account.pubkey,
            tokenMetadata = tokenMetadata,
            price = price
        )
    }

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
            tokenServiceAddress = token.tokenServiceAddress,
            extensions = toDatabase(token.tokenExtensions)
        )

    fun toDatabase(tokenExtension: TokenExtensions): TokenExtensionEntity {
        return TokenExtensionEntity(
            ruleOfProcessingTokenPrice = tokenExtension.ruleOfProcessingTokenPrice,
            isTokenVisibleOnWalletScreen = tokenExtension.isTokenVisibleOnWalletScreen,
            isTokenCellVisibleOnWalletScreen = tokenExtension.isTokenCellVisibleOnWalletScreen,
            tokenPercentDifferenceOnWalletScreen = tokenExtension.tokenPercentDifferenceOnWalletScreen,
            isCalculateWithTotalBalance = tokenExtension.isCalculateWithTotalBalance,
            numbersAfterDecimalPoint = tokenExtension.numbersAfterDecimalPoint,
            canTokenBeHidden = tokenExtension.canTokenBeHidden
        )
    }

    fun fromDatabase(entity: TokenEntity): Token.Active = Token.Active(
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
        tokenServiceAddress = entity.tokenServiceAddress,
        tokenExtensions = fromDatabase(entity.extensions)
    )

    private fun fromDatabase(entity: TokenExtensionEntity?): TokenExtensions =
        TokenExtensions(
            ruleOfProcessingTokenPrice = entity?.ruleOfProcessingTokenPrice,
            isTokenVisibleOnWalletScreen = entity?.isTokenVisibleOnWalletScreen,
            isTokenCellVisibleOnWalletScreen = entity?.isTokenCellVisibleOnWalletScreen,
            tokenPercentDifferenceOnWalletScreen = entity?.tokenPercentDifferenceOnWalletScreen,
            isCalculateWithTotalBalance = entity?.isCalculateWithTotalBalance,
            numbersAfterDecimalPoint = entity?.numbersAfterDecimalPoint,
            canTokenBeHidden = entity?.canTokenBeHidden
        )
}
