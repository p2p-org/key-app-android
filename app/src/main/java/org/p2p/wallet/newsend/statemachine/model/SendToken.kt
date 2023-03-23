package org.p2p.wallet.newsend.statemachine.model

import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports

sealed interface SendToken {

    /**
     * support bridge send
     */
    data class Common(
        val token: Token.Active,
    ) : SendToken {

        val tokenAmount = token.totalInLamports.fromLamports(token.decimals)
    }
}
