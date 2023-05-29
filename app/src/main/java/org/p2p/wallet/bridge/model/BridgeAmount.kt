package org.p2p.wallet.bridge.model

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.p2p.core.utils.asApproximateUsd
import org.p2p.core.utils.formatToken
import org.p2p.core.utils.isNullOrZero
import org.p2p.core.utils.orZero
import org.p2p.wallet.utils.emptyString

@Parcelize
data class BridgeAmount(
    val tokenSymbol: String,
    val tokenDecimals: Int,
    val tokenAmount: BigDecimal?,
    val fiatAmount: BigDecimal?
) : Parcelable {

    companion object {
        fun zero(): BridgeAmount = BridgeAmount(
            tokenSymbol = emptyString(),
            tokenDecimals = 0,
            tokenAmount = null,
            fiatAmount = null
        )
    }

    @IgnoredOnParcel
    val isZero: Boolean = tokenAmount.isNullOrZero() || fiatAmount.isNullOrZero()

    val formattedTokenAmount: String?
        get() = tokenAmount?.let { "${it.formatToken(tokenDecimals)} $tokenSymbol" }

    val formattedFiatAmount: String?
        get() = fiatAmount?.asApproximateUsd(withBraces = false)

    operator fun plus(other: BridgeAmount): BridgeAmount {
        if (this.tokenSymbol != other.tokenSymbol) return this
        return copy(
            tokenAmount = tokenAmount.orZero() + other.tokenAmount.orZero(),
            fiatAmount = fiatAmount.orZero() + other.fiatAmount.orZero()
        )
    }
}
