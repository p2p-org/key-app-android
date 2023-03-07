package org.p2p.ethereumkit.external.model

import java.math.BigDecimal
import java.math.RoundingMode
import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.scaleMedium
import org.p2p.core.utils.toPowerValue

object EthTokenConverter {

    fun ethMetadataToToken(metadata: EthTokenMetadata): Token.Eth = Token.Eth(
        publicKey = metadata.contractAddress.hex,
        tokenSymbol = metadata.symbol,
        decimals = metadata.decimals,
        mintAddress = metadata.mintAddress,
        tokenName = metadata.tokenName,
        iconUrl = metadata.logoUrl,
        totalInUsd = metadata.price.takeIf { it.isNotZero() }
            ?.let { metadata.balance.fromLamports(metadata.decimals).times(it).scaleMedium() },
        total = BigDecimal(metadata.balance).divide(metadata.decimals.toPowerValue(), RoundingMode.HALF_UP),
        rate = metadata.price,
    )

}
