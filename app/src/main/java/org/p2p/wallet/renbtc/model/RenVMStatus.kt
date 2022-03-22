package org.p2p.wallet.renbtc.model

import android.content.Context
import org.p2p.wallet.R
import org.p2p.wallet.utils.scaleMedium
import java.math.BigDecimal

sealed class RenVMStatus(
    open val date: Long
) {

    fun getStringResId(context: Context): String =
        when (this) {
            is Active -> context.getString(R.string.receive_started_session)
            is WaitingDepositConfirm -> context.getString(R.string.receive_waiting_for_deposit)
            is SubmittingToRenVM -> context.getString(R.string.receive_submitting_to_renvm)
            is AwaitingForSignature -> context.getString(R.string.receive_awaiting_the_signature)
            is Minting -> context.getString(R.string.receive_minting)
            is SuccessfullyMinted -> context.getString(R.string.receive_successfully_minted, this.amount.scaleMedium())
        }

    data class Active(override val date: Long) : RenVMStatus(date)
    object WaitingDepositConfirm : RenVMStatus(System.currentTimeMillis())
    object SubmittingToRenVM : RenVMStatus(System.currentTimeMillis())
    object AwaitingForSignature : RenVMStatus(System.currentTimeMillis())
    object Minting : RenVMStatus(System.currentTimeMillis())
    data class SuccessfullyMinted(val amount: BigDecimal) : RenVMStatus(System.currentTimeMillis()) {
        fun getMintedData(): String = "+ ${amount.scaleMedium()} renBTC"
    }
}
