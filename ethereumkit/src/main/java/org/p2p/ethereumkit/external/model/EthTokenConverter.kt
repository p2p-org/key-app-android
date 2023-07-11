package org.p2p.ethereumkit.external.model

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports

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
        val totalInUsd = fiatAmount ?: tokenRate?.let { metadata.balance
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
            latestActiveBundleId = bundleId
        )
    }
}
