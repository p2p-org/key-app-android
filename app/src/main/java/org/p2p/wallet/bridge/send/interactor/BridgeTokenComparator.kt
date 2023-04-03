package org.p2p.wallet.bridge.send.interactor

import org.p2p.core.token.Token
import org.p2p.core.utils.isMoreThan
import org.p2p.ethereumkit.external.model.ERC20Tokens

class BridgeTokenComparator : Comparator<Token> {

    override fun compare(o1: Token, o2: Token): Int = when {
        o1.isUsdcEt() -> -1
        o2.isUsdcEt() -> 1
        o1.isUSDT -> -1
        o2.isUSDT -> 1
        o1 is Token.Active && o2 is Token.Active -> compareActiveTokens(o1, o2)
        o1 is Token.Active -> -1
        o2 is Token.Active -> 1
        else -> 0
    }

    private fun compareActiveTokens(
        o1: Token.Active,
        o2: Token.Active
    ): Int = when {
        o1.totalInUsd != null && o2.totalInUsd != null -> if (o1.totalInUsd!!.isMoreThan(o2.totalInUsd!!)) -1 else 1
        o1.totalInUsd != null && o2.totalInUsd == null -> -1
        o1.totalInUsd == null && o2.totalInUsd != null -> 1
        else -> 0
    }

    private fun Token.isUsdcEt(): Boolean = mintAddress == ERC20Tokens.USDC.mintAddress
}
