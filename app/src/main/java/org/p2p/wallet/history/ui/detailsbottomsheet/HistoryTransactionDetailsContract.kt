package org.p2p.wallet.history.ui.detailsbottomsheet

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.transaction.model.HistoryTransactionStatus
import org.p2p.wallet.utils.Base58String

interface HistoryTransactionDetailsContract {

    interface View : MvpView {
        fun showTransferView(@DrawableRes iconRes: Int)
        fun showSwapView(sourceIconUrl: String, destinationIconUrl: String)
        fun showDate(date: String)
        fun showStatus(status: HistoryTransactionStatus)
        fun showTransactionId(signature: String)
        fun showAmount(amountToken: String, amountUsd: String?)
        fun showFee(renBtcFee: String? = null)
        fun showLoading(isLoading: Boolean)
        fun showError(@StringRes messageId: Int)
        fun showSenderAddress(senderAddress: Base58String, senderUsername: String?)
        fun showReceiverAddress(receiverAddress: Base58String, receiverUsername: String?)
        fun showAddresses(source: String, destination: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun load(transactionId: String)
    }
}
