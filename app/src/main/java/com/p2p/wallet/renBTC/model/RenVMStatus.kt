package com.p2p.wallet.renBTC.model

import com.p2p.wallet.R
import java.math.BigDecimal

sealed class RenVMStatus(
    open val date: Long
) {

    fun getStringResId(): Int =
        when (this) {
            is Active -> R.string.receive_started_session
            is WaitingDepositConfirm -> R.string.receive_waiting_for_deposit
            is SubmittingToRenVM -> R.string.receive_submitting_to_renvm
            is AwaitingForSignature -> R.string.receive_awaiting_the_signature
            is MinAmountReceived -> R.string.receive_min_transaction_amount
            is Minting -> R.string.receive_minting
            is SuccessfullyMinted -> R.string.receive_successfully_minted
        }

    data class Active(override val date: Long) : RenVMStatus(date)
    object WaitingDepositConfirm : RenVMStatus(System.currentTimeMillis())
    object SubmittingToRenVM : RenVMStatus(System.currentTimeMillis())
    object AwaitingForSignature : RenVMStatus(System.currentTimeMillis())
    data class MinAmountReceived(val amount: String) : RenVMStatus(System.currentTimeMillis())
    object Minting : RenVMStatus(System.currentTimeMillis())
    data class SuccessfullyMinted(val amount: BigDecimal) : RenVMStatus(System.currentTimeMillis())
}