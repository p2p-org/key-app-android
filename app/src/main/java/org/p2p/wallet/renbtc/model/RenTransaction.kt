package org.p2p.wallet.renbtc.model

import android.content.Context
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.core.utils.isNotZero
import org.p2p.core.utils.scaleMedium
import org.p2p.wallet.R
import org.p2p.wallet.renbtc.model.RenTransactionStatus.WaitingDepositConfirm

@Parcelize
data class RenTransaction(
    val transactionId: String,
    val payment: RenBTCPayment,
    val statuses: MutableList<RenTransactionStatus> = mutableListOf(WaitingDepositConfirm(transactionId))
) : Parcelable {

    fun getTransactionTitle(context: Context): String {
        val currentStatus = statuses.lastOrNull()
        return if (currentStatus is RenTransactionStatus.SuccessfullyMinted) {
            val amount = currentStatus.amount
            val scaleMedium = if (amount.isNotZero()) amount.scaleMedium() else "N/A"
            context.getString(R.string.receive_renbtc_format, scaleMedium)
        } else {
            context.getString(R.string.main_mint)
        }
    }

    fun getLatestStatus(): RenTransactionStatus? = statuses.lastOrNull()

    fun isAwaiting() = statuses.lastOrNull() is WaitingDepositConfirm

    fun isFinished(): Boolean = statuses.lastOrNull() is RenTransactionStatus.SuccessfullyMinted

    fun isActive(): Boolean {
        val latestStatus = statuses.lastOrNull()
        val isMinted = latestStatus is RenTransactionStatus.SuccessfullyMinted
        val isSubmitting = latestStatus is RenTransactionStatus.SubmittingToRenVM
        val isMinting = latestStatus is RenTransactionStatus.Minting
        val isAwaiting = latestStatus is RenTransactionStatus.AwaitingForSignature
        return isMinted || isSubmitting || isMinting || isAwaiting
    }
}
