package org.p2p.wallet.history.ui.detailsbottomsheet

import android.text.SpannableString
import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.bottomsheet.DrawableContainer
import org.p2p.wallet.transaction.model.TransactionStatus

interface TransactionDetailsBottomSheetContract {

    interface View : MvpView {
        fun showDate(date: String)
        fun showStatus(status: TransactionStatus)
        fun showSignature(signature: String)
        fun showAddresses(source: String, destination: String)
        fun showAmount(amountToken: String, amountUsd: String?)
        fun showFee(renBtcFee: String? = null)
        fun showBlockNumber(blockNumber: String?)
        fun showLoading(isLoading: Boolean)
        fun showError(@StringRes messageId: Int)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
    }
}
