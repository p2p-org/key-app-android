package org.p2p.wallet.claim.model

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.scaleMedium
import org.p2p.wallet.utils.emptyString

@Parcelize
data class BridgeAmount(
    val tokenSymbol: String,
    val tokenAmount: BigDecimal?,
    val fiatAmount: BigDecimal?
) : Parcelable {

    companion object {
        fun zero(): BridgeAmount = BridgeAmount(
            tokenSymbol = emptyString(),
            tokenAmount = null,
            fiatAmount = null
        )
    }

    val formattedTokenAmount: String?
        get() = tokenAmount?.let { "${it.scaleMedium().formatToken()} $tokenSymbol" }

    val formattedFiatAmount: String?
        get() = fiatAmount?.asApproximateUsd(withBraces = false)
}
