package org.p2p.wallet.bridge.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.core.utils.orZero
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
    val amountInToken
        get() = amount?.toBigDecimal()?.orZero()?.divide(decimals.toPowerValue()).orZero()
}
