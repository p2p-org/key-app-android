package org.p2p.wallet.bridge.model

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize
import org.p2p.core.utils.isNullOrZero
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toBigDecimalOrZero
import org.p2p.core.utils.toPowerValue
import org.p2p.core.wrapper.eth.EthAddress

@Parcelize
data class BridgeFee(
    val amount: String?,
    val amountInUsd: String?,
    val chain: String?,
    val token: EthAddress?,
    val symbol: String,
    val name: String,
    val decimals: Int
) : Parcelable {
    val amountInToken: BigDecimal
        get() = amount?.toBigDecimal()?.let { it.divide(decimals.toPowerValue()) }.orZero()
}

fun BridgeFee?.toBridgeAmount(): BridgeAmount {
    return BridgeAmount(
        tokenSymbol = this?.symbol.orEmpty(),
        tokenDecimals = this?.decimals.orZero(),
        tokenAmount = this?.amountInToken?.takeIf { !it.isNullOrZero() },
        fiatAmount = this?.amountInUsd?.toBigDecimalOrZero()
    )
}
