package org.p2p.wallet.bridge.send.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.bridge.model.BridgeFee

@Parcelize
data class BridgeSendFees(
    val networkFee: BridgeFee,
    val networkFeeInToken: BridgeFee,
    val messageAccountRent: BridgeFee,
    val messageAccountRentInToken: BridgeFee,
    val bridgeFee: BridgeFee,
    val bridgeFeeInToken: BridgeFee,
    val arbiterFee: BridgeFee,
    val totalAmount: BridgeFee,
    val recipientGetsAmount: BridgeFee,
) : Parcelable {
    fun getFeeList(): List<BridgeFee> {
        return listOf(
            arbiterFee,
            bridgeFeeInToken,
            networkFeeInToken,
        )
    }
}

fun BridgeSendFees?.getFeeList(): List<BridgeFee> {
    return this?.getFeeList() ?: emptyList()
}
