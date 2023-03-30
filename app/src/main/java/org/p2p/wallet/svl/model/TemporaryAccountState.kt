package org.p2p.wallet.svl.model

import java.math.BigInteger
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.newsend.model.TemporaryAccount

sealed interface TemporaryAccountState {

    data class Active(
        val account: TemporaryAccount,
        val amountInLamports: BigInteger,
        val tokenSymbol: String,
        val tokenDecimals: Int,
        val tokenIconUrl: String?
    ) : TemporaryAccountState {

        val amountInTokens: String
            get() = amountInLamports.fromLamports(tokenDecimals).toPlainString()
    }

    object EmptyBalance : TemporaryAccountState

    object ParsingFailed : TemporaryAccountState
}
