package org.p2p.wallet.send.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.send.model.send_service.GeneratedTransaction

@Parcelize
data class SendServiceFees(
    val recipientGetsAmount: GeneratedTransaction.AmountData,
    val totalAmount: GeneratedTransaction.AmountData,
    val networkFee: GeneratedTransaction.NetworkFeeData,
    val tokenAccountRent: GeneratedTransaction.NetworkFeeData,
    val token2022TransferFee: GeneratedTransaction.NetworkFeeData,
) : Parcelable
