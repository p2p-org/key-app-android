package org.p2p.wallet.bridge.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BridgeBundleFees(
    val gasFee: BridgeFee,
    val gasFeeInToken: BridgeFee,
    val arbiterFee: BridgeFee,
    val createAccount: BridgeFee,
) : Parcelable
