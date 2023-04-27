package org.p2p.wallet.bridge.send.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.bridge.model.BridgeAmount

@Parcelize
data class BridgeFeeDetails(
    val recipientAddress: String,
    val willGetAmount: BridgeAmount,
    val networkFee: BridgeAmount,
    val messageAccountRent: BridgeAmount,
    val bridgeFee: BridgeAmount
) : Parcelable
