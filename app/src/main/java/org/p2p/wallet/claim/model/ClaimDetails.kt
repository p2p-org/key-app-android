package org.p2p.wallet.claim.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClaimDetails(
    val willGetAmount: BridgeAmount,
    val networkFee: BridgeAmount,
    val accountCreationFee: BridgeAmount,
    val bridgeFee: BridgeAmount,
) : Parcelable
