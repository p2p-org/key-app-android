package org.p2p.ethereumkit.external.model

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.token.service.model.TokenServiceMetadata

object EthTokenConverter {

    fun ethMetadataToToken(
        metadata: EthTokenMetadata,
        isClaiming: Boolean = false,
        bundleId: String?,
        tokenAmount: BigDecimal?,
        fiatAmount: BigDecimal?,
    ): Token.Eth {

        val tokenRate = metadata.price
        val total = tokenAmount ?: metadata.balance.fromLamports(metadata.decimals)
        val totalInUsd = fiatAmount ?: tokenRate?.let {
            metadata.balance
                .fromLamports(metadata.decimals)
                .times(it)
        }
        return Token.Eth(
            publicKey = metadata.contractAddress.hex,
            tokenSymbol = metadata.symbol,
            decimals = metadata.decimals,
            mintAddress = metadata.mintAddress,
            tokenName = metadata.tokenName,
            iconUrl = metadata.logoUrl,
            totalInUsd = totalInUsd,
            total = total,
            rate = metadata.price,
            isClaiming = isClaiming,
            latestActiveBundleId = bundleId,
            tokenServiceAddress = metadata.tokenServiceAddress
        )
    }

    fun toEthTokenMetadata(
        ethAddress: String,
        metadata: TokenServiceMetadata,
        tokenBalance: BigInteger
    ): EthTokenMetadata {
        val erc20Token = ERC20Tokens.findToken(ethAddress)
        return EthTokenMetadata(
            contractAddress = EthAddress(erc20Token.contractAddress),
            mintAddress = erc20Token.mintAddress,
            balance = tokenBalance,
            decimals = metadata.decimals,
            logoUrl = erc20Token.tokenIconUrl,
            tokenName = metadata.name,
            symbol = erc20Token.replaceTokenSymbol.orEmpty(),
            price = null
        )
    }

    fun createNativeEthMetadata(
        ethAddress: String,
        metadata: TokenServiceMetadata,
        tokenBalance: BigInteger
    ): EthTokenMetadata {
        val erc20Token = ERC20Tokens.findToken(ethAddress)
        return EthTokenMetadata(
            contractAddress = EthAddress(ethAddress),
            mintAddress = erc20Token.mintAddress,
            balance = tokenBalance,
            decimals = metadata.decimals,
            logoUrl = erc20Token.tokenIconUrl,
            tokenName = metadata.name,
            symbol = erc20Token.replaceTokenSymbol.orEmpty(),
            price = null
        )
    }
}
