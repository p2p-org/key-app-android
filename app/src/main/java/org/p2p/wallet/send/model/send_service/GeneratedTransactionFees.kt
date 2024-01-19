package org.p2p.wallet.send.model.send_service

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize

sealed interface GeneratedTransactionFees {
    @Parcelize
    data class Result(
        val recipientGetsAmount: GeneratedTransaction.AmountData,
        val totalAmount: GeneratedTransaction.AmountData,
        val networkFee: GeneratedTransaction.AmountData,
        val tokenAccountRent: GeneratedTransaction.AmountData,
        val token2022TransferFee: GeneratedTransaction.AmountData,
        val token2022TransferFeePercent: BigDecimal?,
        val token2022InterestBearingPercent: BigDecimal?,
    ) : Parcelable, GeneratedTransactionFees

    @Parcelize
    data class Error(
        val throwable: Throwable? = null,
        val message: String? = "Unknown error"
    ) : Parcelable, GeneratedTransactionFees
}
