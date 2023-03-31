package org.p2p.wallet.bridge.send.mapper

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.core.token.TokenVisibility

class SendUiMapper() {
    fun toTokenActiveStub(token: Token.Other): Token.Active {
        return Token.Active(
            publicKey = token.publicKey.orEmpty(),
            totalInUsd = BigDecimal.ZERO,
            total = BigDecimal.ZERO,
            tokenSymbol = token.tokenSymbol,
            decimals = token.decimals,
            mintAddress = token.mintAddress,
            tokenName = token.tokenName,
            iconUrl = token.iconUrl,
            rate = null,
            visibility = TokenVisibility.DEFAULT,
            serumV3Usdc = token.serumV3Usdc,
            serumV3Usdt = token.serumV3Usdt,
            isWrapped = token.isWrapped
        )
    }
}
