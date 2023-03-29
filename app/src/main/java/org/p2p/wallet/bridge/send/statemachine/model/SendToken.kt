package org.p2p.wallet.bridge.send.statemachine.model

import org.p2p.core.token.Token
import org.p2p.core.utils.fromLamports

sealed interface SendToken {

    data class Bridge(
        val token: Token.Active,
    ) : SendToken {

        val tokenAmount = token.totalInLamports.fromLamports(token.decimals)
    }
}
