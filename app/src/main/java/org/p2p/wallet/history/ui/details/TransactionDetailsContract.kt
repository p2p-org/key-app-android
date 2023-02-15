package org.p2p.wallet.history.ui.details

import android.text.SpannableString
import androidx.annotation.StringRes
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.common.ui.bottomsheet.DrawableContainer
import org.p2p.wallet.transaction.model.HistoryTransactionStatus

interface TransactionDetailsContract {

    interface View : MvpView {
        fun showTitle(title: String)
        fun showDate(date: String)
        fun showStatus(status: HistoryTransactionStatus)
        fun showSourceInfo(iconContainer: DrawableContainer, primaryInfo: String, secondaryInfo: String?)
        fun showDestinationInfo(iconContainer: DrawableContainer, primaryInfo: String, secondaryInfo: String?)
        fun showSignature(signature: String)
        fun showAddresses(source: String, destination: String)
        fun showAmount(@StringRes label: Int, amount: CharSequence)
        fun showLiquidityProviderFees(sourceFee: SpannableString, destinationFee: SpannableString)
        fun showFee(renBtcFee: String?)
        fun showBlockNumber(blockNumber: String?)
        fun showLoading(isLoading: Boolean)
        fun showError(@StringRes messageId: Int)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
    }
}
