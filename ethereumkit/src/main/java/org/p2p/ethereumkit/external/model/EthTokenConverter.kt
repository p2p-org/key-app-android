package org.p2p.ethereumkit.external.model

import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports

object EthTokenConverter {

    fun ethMetadataToToken(metadata: EthTokenMetadata): Token.Eth = Token.Eth(
        publicKey = metadata.contractAddress.hex,
        tokenSymbol = metadata.symbol,
        decimals = metadata.decimals,
        mintAddress = metadata.mintAddress,
        tokenName = metadata.tokenName,
        iconUrl = metadata.logoUrl,
        totalInUsd = metadata.balance.fromLamports(metadata.decimals).times(metadata.price),
        total = metadata.balance.fromLamports(metadata.decimals),
        rate = metadata.price,
    )
}
