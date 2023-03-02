package org.p2p.wallet.newsend.interactor

import java.math.BigInteger
import org.p2p.core.token.Token
import org.p2p.solanaj.core.PublicKey

class SendViaLinkInteractor(
    private val sendInteractor: SendInteractor
) {

    fun createSendTransaction(
        destinationAddress: PublicKey,
        token: Token.Active,
        lamports: BigInteger
    ) {
        // tbd
    }
}
