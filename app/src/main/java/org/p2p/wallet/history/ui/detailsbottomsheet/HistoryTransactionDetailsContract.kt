package org.p2p.wallet.history.ui.detailsbottomsheet

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import org.threeten.bp.ZonedDateTime
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.utils.Base58String

interface HistoryTransactionDetailsContract {

    interface View : MvpView {
        fun showTransferView(tokenIconUrl: String?, placeholderIcon: Int)
        fun showSwapView(sourceIconUrl: String?, destinationIconUrl: String?)
        fun showDate(date: ZonedDateTime)
        fun showStatus(@StringRes titleResId: Int, @ColorRes colorId: Int)
        fun showErrorState(errorMessage: String)
        fun showTransactionId(signature: String)
        fun showAmount(amountToken: String?, amountUsd: String?)
        fun showFee(fees: String? = null)
        fun showLoading(isLoading: Boolean)
        fun showError(@StringRes messageId: Int)
        fun showSenderAddress(senderAddress: Base58String, senderUsername: String?)
        fun showReceiverAddress(receiverAddress: Base58String, receiverUsername: String?)
        fun showStateTitleValue(title: String, value: String)
        fun hideSendReceiveTitleAndValue()
    }

    interface Presenter : MvpPresenter<View> {
        fun load(transactionId: String)
    }
}
