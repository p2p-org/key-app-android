package org.p2p.wallet.renbtc.model

import android.content.Context
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.R
import org.p2p.wallet.utils.isNotZero
import org.p2p.wallet.utils.scaleMedium
import java.math.BigDecimal

/* Five minutes */
private const val ONE_MINUTE_IN_MS = 1000 * 60 * 5
private const val NA = "N/A"

sealed class RenTransactionStatus(
    open val transactionId: String,
    open val date: Long
) : Parcelable {

    @Parcelize
    data class WaitingDepositConfirm(
        override val transactionId: String
    ) : RenTransactionStatus(transactionId, System.currentTimeMillis())

    @Parcelize
    data class SubmittingToRenVM(
        override val transactionId: String
    ) : RenTransactionStatus(transactionId, System.currentTimeMillis())

    @Parcelize
    data class AwaitingForSignature(
        override val transactionId: String
    ) : RenTransactionStatus(transactionId, System.currentTimeMillis())

    @Parcelize
    data class Minting(
        override val transactionId: String
    ) : RenTransactionStatus(transactionId, System.currentTimeMillis())

    @Parcelize
    data class SuccessfullyMinted(
        override val transactionId: String,
        val amount: BigDecimal
    ) : RenTransactionStatus(transactionId, System.currentTimeMillis()) {
        fun getMintedData(): String {
            val value = if (amount.isNotZero()) amount.scaleMedium() else NA
            return "+ $value renBTC"
        }
    }

    fun isSuccessAndPastMinuteAgo(): Boolean =
        this is SuccessfullyMinted && this.date - System.currentTimeMillis() > ONE_MINUTE_IN_MS

    fun getStringResId(context: Context): String =
        when (this) {
            is WaitingDepositConfirm -> context.getString(R.string.receive_waiting_for_deposit)
            is SubmittingToRenVM -> context.getString(R.string.receive_submitting_to_renvm)
            is AwaitingForSignature -> context.getString(R.string.receive_awaiting_the_signature)
            is Minting -> context.getString(R.string.receive_minting)
            is SuccessfullyMinted -> context.getString(
                R.string.receive_successfully_minted,
                if (amount.isNotZero()) amount.scaleMedium() else NA
            )
        }
}
