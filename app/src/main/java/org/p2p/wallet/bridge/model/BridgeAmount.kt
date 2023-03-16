package org.p2p.wallet.bridge.model

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isNullOrZero
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

    @IgnoredOnParcel
    val isFree: Boolean = tokenAmount.isNullOrZero() || fiatAmount.isNullOrZero()

    val formattedTokenAmount: String?
        get() = tokenAmount?.let { "${it.scaleMedium().formatToken()} $tokenSymbol" }

    val formattedFiatAmount: String?
        get() = fiatAmount?.asApproximateUsd(withBraces = false)
}
